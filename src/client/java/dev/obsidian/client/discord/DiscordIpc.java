package dev.obsidian.client.discord;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

/**
 * Minimal Discord Rich Presence client that talks to the local Discord app over its IPC socket
 * (Unix socket on Linux/macOS, named pipe on Windows). No external libraries needed.
 */
public final class DiscordIpc implements AutoCloseable {
	private static final int OP_HANDSHAKE = 0, OP_FRAME = 1;

	private interface Conn extends AutoCloseable {
		void write(byte[] b) throws IOException;
		void readFully(byte[] b) throws IOException;
		@Override void close() throws IOException;
	}

	private Conn conn;
	private int nonce;

	/** Tries discord-ipc-0..9. Returns false if Discord is not running. */
	public boolean connect(String clientId) {
		for (int i = 0; i < 10 && conn == null; i++) {
			try {
				conn = open(i);
			} catch (Exception ignored) {}
		}
		if (conn == null) return false;
		try {
			JsonObject hs = new JsonObject();
			hs.addProperty("v", 1);
			hs.addProperty("client_id", clientId);
			send(OP_HANDSHAKE, hs.toString());
			readFrame();
			return true;
		} catch (Exception e) {
			close();
			return false;
		}
	}

	public boolean isConnected() { return conn != null; }

	/** Sends a SET_ACTIVITY command. Pass activity == null to clear. */
	public void setActivity(JsonObject activity) throws IOException {
		JsonObject args = new JsonObject();
		args.addProperty("pid", ProcessHandle.current().pid());
		if (activity != null) args.add("activity", activity);
		JsonObject cmd = new JsonObject();
		cmd.addProperty("cmd", "SET_ACTIVITY");
		cmd.add("args", args);
		cmd.addProperty("nonce", Integer.toString(nonce++));
		send(OP_FRAME, cmd.toString());
		readFrame(); // Discord answers every command
	}

	private void send(int op, String json) throws IOException {
		byte[] data = json.getBytes(StandardCharsets.UTF_8);
		ByteBuffer buf = ByteBuffer.allocate(8 + data.length).order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(op).putInt(data.length).put(data);
		conn.write(buf.array());
	}

	private String readFrame() throws IOException {
		byte[] header = new byte[8];
		conn.readFully(header);
		int len = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
		if (len < 0 || len > 1 << 20) throw new IOException("bad frame");
		byte[] body = new byte[len];
		conn.readFully(body);
		return new String(body, StandardCharsets.UTF_8);
	}

	@Override
	public void close() {
		try { if (conn != null) conn.close(); } catch (IOException ignored) {}
		conn = null;
	}

	// ---- platform-specific connections ----

	private static Conn open(int index) throws IOException {
		String name = "discord-ipc-" + index;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			RandomAccessFile f = new RandomAccessFile("\\\\?\\pipe\\" + name, "rw");
			return new Conn() {
				public void write(byte[] b) throws IOException { f.write(b); }
				public void readFully(byte[] b) throws IOException { f.readFully(b); }
				public void close() throws IOException { f.close(); }
			};
		}
		for (Path dir : socketDirs()) {
			Path p = dir.resolve(name);
			if (!Files.exists(p)) continue;
			SocketChannel ch = SocketChannel.open(StandardProtocolFamily.UNIX);
			try {
				ch.connect(UnixDomainSocketAddress.of(p));
			} catch (IOException e) {
				ch.close();
				continue;
			}
			return new Conn() {
				public void write(byte[] b) throws IOException {
					ByteBuffer bb = ByteBuffer.wrap(b);
					while (bb.hasRemaining()) ch.write(bb);
				}
				public void readFully(byte[] b) throws IOException {
					ByteBuffer bb = ByteBuffer.wrap(b);
					while (bb.hasRemaining()) if (ch.read(bb) < 0) throw new IOException("closed");
				}
				public void close() throws IOException { ch.close(); }
			};
		}
		throw new IOException("no socket");
	}

	private static List<Path> socketDirs() {
		List<Path> dirs = new ArrayList<>();
		for (String env : new String[] { "XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP" }) {
			String v = System.getenv(env);
			if (v == null || v.isEmpty()) continue;
			Path base = Path.of(v);
			dirs.add(base);
			dirs.add(base.resolve("app/com.discordapp.Discord"));            // Flatpak
			dirs.add(base.resolve("snap.discord"));                           // Snap
		}
		dirs.add(Path.of("/tmp"));
		return dirs;
	}
}
