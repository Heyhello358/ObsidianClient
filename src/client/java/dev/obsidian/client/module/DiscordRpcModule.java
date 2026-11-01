package dev.obsidian.client.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.obsidian.client.discord.DiscordIpc;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;

/** Discord Rich Presence with a "Join Discord" button. */
public class DiscordRpcModule extends Module {
	public static final String APP_ID = "1566475880669380678";
	public static final String INVITE = "https://discord.gg/XAj6Xb9t9A";

	private volatile String details = "In the menus";
	private volatile String state = "Obsidian Client";
	private volatile boolean running;
	private Thread thread;
	private int ticks;

	public DiscordRpcModule() {
		super("discord", "Discord RPC", "Shows Obsidian Client on your Discord profile", Category.CLIENT, 0, 0);
	}

	@Override protected void onEnable() {
		if (running) return;
		running = true;
		long start = System.currentTimeMillis() / 1000;
		thread = new Thread(() -> loop(start), "Obsidian-DiscordRPC");
		thread.setDaemon(true);
		thread.start();
	}

	@Override protected void onDisable() {
		running = false;
		if (thread != null) thread.interrupt();
	}

	@Override public void tick() {
		if (ticks++ % 40 != 0) return;
		Minecraft mc = Minecraft.getInstance();
		details = mc.level == null ? "In the menus"
			: mc.getCurrentServer() != null ? "Playing multiplayer" : "Playing singleplayer";
		state = "Minecraft " + SharedConstants.getCurrentVersion().name();
	}

	private void loop(long start) {
		try (DiscordIpc ipc = new DiscordIpc()) {
			while (running) {
				if (!ipc.isConnected() && !ipc.connect(APP_ID)) { Thread.sleep(15_000); continue; }
				try { ipc.setActivity(buildActivity(start)); } catch (Exception e) { ipc.close(); }
				Thread.sleep(15_000);
			}
			if (ipc.isConnected()) ipc.setActivity(null);
		} catch (InterruptedException ignored) {
		} catch (Exception e) {
			System.err.println("[Obsidian] Discord RPC stopped: " + e);
		}
	}

	private JsonObject buildActivity(long start) {
		JsonObject a = new JsonObject();
		a.addProperty("details", details);
		a.addProperty("state", state);
		JsonObject ts = new JsonObject(); ts.addProperty("start", start); a.add("timestamps", ts);
		JsonObject assets = new JsonObject();
		assets.addProperty("large_image", "logo");
		assets.addProperty("large_text", "Obsidian Client");
		a.add("assets", assets);
		JsonObject button = new JsonObject();
		button.addProperty("label", "Join Discord");
		button.addProperty("url", INVITE);
		JsonArray buttons = new JsonArray(); buttons.add(button); a.add("buttons", buttons);
		return a;
	}
}
