package dev.obsidian.client.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.obsidian.client.module.NametagModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

/**
 * Talks to the Obsidian backend. Your identity is proven through Mojang (joinServer): your access token
 * goes only to Mojang, never to our server. We only send your username and a one-time server id.
 */
public final class ObsidianNet {
	public record Tag(Role role, int color) {}
	public record Entry(String uuid, String name, Role role, String color, boolean online) {}

	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
	private static final Map<UUID, Tag> TAGS = new ConcurrentHashMap<>();
	private static final SecureRandom RNG = new SecureRandom();

	public static volatile boolean admin;
	public static volatile Role myRole = Role.MEMBER;
	public static volatile String status = "Not connected";
	public static volatile List<Entry> adminList = List.of();
	public static volatile String adminMsg = "";

	private static volatile String token;
	private static volatile long tokenExp;
	private static volatile List<String> watch = List.of();
	private static boolean running;

	private ObsidianNet() {}

	public static Tag tag(UUID id) { return TAGS.get(id); }

	public static synchronized void start() {
		if (running) return;
		running = true;
		Thread t = new Thread(ObsidianNet::loop, "Obsidian-Net");
		t.setDaemon(true);
		t.start();
	}

	/** Main thread: remember who is around so the worker can ask for their tags. */
	public static void tick(Minecraft mc) {
		if (mc.level == null) { watch = List.of(); return; }
		List<String> ids = new ArrayList<>();
		for (var p : mc.level.players()) ids.add(p.getUUID().toString().replace("-", ""));
		watch = ids;
	}

	// ---- background loop ----
	private static void loop() {
		long lastBeat = 0, lastTags = 0;
		while (true) {
			try {
				NametagModule m = NametagModule.instance;
				if (!Backend.configured()) { status = "Backend not set up yet"; Thread.sleep(10_000); continue; }
				if (m == null || !m.isEnabled()) { status = "Obsidian Nametags is off"; TAGS.clear(); Thread.sleep(5_000); continue; }
				long now = System.currentTimeMillis();
				if (m.share() && now - lastBeat > 300_000) {
					JsonObject r = JsonParser.parseString(call("POST", "/heartbeat", "{}", true)).getAsJsonObject();
					myRole = Role.of(r.get("role").getAsString());
					status = "Connected - role: " + myRole.label;
					lastBeat = now;
				} else if (!m.share()) {
					status = "Connected (your logo is hidden)";
				}
				if (m.showOthers() && now - lastTags > 12_000 && !watch.isEmpty()) {
					fetchTags();
					lastTags = now;
				}
				Thread.sleep(1_000);
			} catch (InterruptedException e) {
				return;
			} catch (Exception e) {
				status = "Offline: " + e.getMessage();
				try { Thread.sleep(15_000); } catch (InterruptedException ie) { return; }
			}
		}
	}

	private static void fetchTags() throws Exception {
		List<String> ids = watch;
		String res = call("GET", "/tags?u=" + String.join(",", ids), null, false);
		JsonObject o = JsonParser.parseString(res).getAsJsonObject();
		Map<UUID, Tag> fresh = new ConcurrentHashMap<>();
		for (String key : o.keySet()) {
			JsonObject t = o.getAsJsonObject(key);
			UUID id = fromNoDash(key);
			if (id == null) continue;
			JsonElement c = t.get("c");
			int color = (c == null || c.isJsonNull()) ? -1 : parseColor(c.getAsString());
			fresh.put(id, new Tag(Role.of(t.get("r").getAsString()), color));
		}
		TAGS.keySet().removeIf(k -> !fresh.containsKey(k));
		TAGS.putAll(fresh);
	}

	// ---- login + http ----
	private static synchronized void ensureLogin() throws Exception {
		if (token != null && System.currentTimeMillis() < tokenExp - 60_000) return;
		Minecraft mc = Minecraft.getInstance();
		User u = mc.getUser();
		byte[] b = new byte[16];
		RNG.nextBytes(b);
		StringBuilder sid = new StringBuilder();
		for (byte x : b) sid.append(String.format("%02x", x));
		try {
			mc.services().sessionService().joinServer(u.getProfileId(), u.getAccessToken(), sid.toString());
		} catch (Exception e) {
			throw new Exception("sign in with a Microsoft account to use this");
		}
		JsonObject body = new JsonObject();
		body.addProperty("username", u.getName());
		body.addProperty("serverId", sid.toString());
		HttpRequest r = HttpRequest.newBuilder(URI.create(Backend.BASE_URL + "/login"))
			.timeout(Duration.ofSeconds(10)).header("User-Agent", "ObsidianClient").header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
		HttpResponse<String> res = HTTP.send(r, HttpResponse.BodyHandlers.ofString());
		if (res.statusCode() != 200) throw new Exception("login failed (" + res.statusCode() + ")");
		JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
		token = o.get("token").getAsString();
		tokenExp = o.get("exp").getAsLong();
		myRole = Role.of(o.get("role").getAsString());
		admin = o.get("admin").getAsBoolean();
	}

	private static String call(String method, String path, String body, boolean auth) throws Exception {
		if (auth) ensureLogin();
		HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(Backend.BASE_URL + path))
			.timeout(Duration.ofSeconds(10)).header("User-Agent", "ObsidianClient").header("Content-Type", "application/json");
		if (auth) b.header("Authorization", "Bearer " + token);
		b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
		HttpResponse<String> r = HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
		if (r.statusCode() == 401 && auth) { token = null; throw new Exception("session expired, try again"); }
		if (r.statusCode() / 100 != 2) throw new Exception("server said " + r.statusCode());
		return r.body();
	}

	// ---- admin (the server checks your identity again, so this only works for the admin) ----
	public static void adminRefresh() {
		async(() -> {
			String res = call("GET", "/admin/list", null, true);
			JsonArray arr = JsonParser.parseString(res).getAsJsonObject().getAsJsonArray("players");
			List<Entry> out = new ArrayList<>();
			for (JsonElement e : arr) {
				JsonObject p = e.getAsJsonObject();
				out.add(new Entry(p.get("uuid").getAsString(),
					p.has("name") && !p.get("name").isJsonNull() ? p.get("name").getAsString() : "",
					Role.of(p.get("role").getAsString()),
					p.has("color") && !p.get("color").isJsonNull() ? p.get("color").getAsString() : "",
					p.get("online").getAsBoolean()));
			}
			adminList = out;
			adminMsg = out.size() + " players";
		});
	}

	public static void adminSet(String uuidText, Role role, String colorText) {
		String u = uuidText.trim().toLowerCase().replace("-", "");
		String c = colorText.trim().replace("#", "");
		if (!u.matches("[0-9a-f]{32}")) { adminMsg = "That is not a valid UUID"; return; }
		if (!c.isEmpty() && !c.matches("[0-9a-fA-F]{6}")) { adminMsg = "Colour must be like #FF0000"; return; }
		JsonObject body = new JsonObject();
		body.addProperty("uuid", u);
		body.addProperty("role", role.id);
		body.addProperty("color", c.isEmpty() ? null : c);
		async(() -> { call("POST", "/admin/set", body.toString(), true); adminMsg = "Saved"; adminRefresh(); });
	}

	public static void adminRemove(String uuid) {
		JsonObject body = new JsonObject();
		body.addProperty("uuid", uuid);
		async(() -> { call("POST", "/admin/remove", body.toString(), true); adminMsg = "Removed"; adminRefresh(); });
	}

	private interface Task { void run() throws Exception; }
	private static void async(Task t) {
		Thread th = new Thread(() -> {
			try { adminMsg = "Working..."; t.run(); } catch (Exception e) { adminMsg = "Error: " + e.getMessage(); }
		}, "Obsidian-Admin");
		th.setDaemon(true);
		th.start();
	}

	private static UUID fromNoDash(String s) {
		if (s.length() != 32) return null;
		try {
			return UUID.fromString(s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16, 20) + "-" + s.substring(20));
		} catch (Exception e) { return null; }
	}

	public static int parseColor(String hex) {
		try { return Integer.parseInt(hex.replace("#", ""), 16) & 0xFFFFFF; } catch (Exception e) { return -1; }
	}
}
