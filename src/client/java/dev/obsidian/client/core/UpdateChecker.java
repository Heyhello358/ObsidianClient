package dev.obsidian.client.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

/** Asks GitHub for the newest release and tells the menu if there is one. */
public final class UpdateChecker {
	public static final String REPO = "Heyhello358/ObsidianClient";
	public static volatile String status = "Checking for updates...";
	public static volatile String latest = "";
	public static volatile boolean updateAvailable;
	public static volatile String url = "https://github.com/" + REPO + "/releases";

	private UpdateChecker() {}

	public static String current() {
		return FabricLoader.getInstance().getModContainer("obsidian")
			.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("0.0.0");
	}

	public static void checkAsync() {
		Thread t = new Thread(UpdateChecker::check, "Obsidian-UpdateCheck");
		t.setDaemon(true);
		t.start();
	}

	private static void check() {
		try {
			HttpClient c = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
			HttpRequest r = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/" + REPO + "/releases/latest"))
				.header("User-Agent", "ObsidianClient").timeout(Duration.ofSeconds(8)).build();
			HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());
			if (res.statusCode() != 200) { status = "No releases found yet"; return; }
			var o = JsonParser.parseString(res.body()).getAsJsonObject();
			latest = o.get("tag_name").getAsString();
			if (o.has("html_url")) url = o.get("html_url").getAsString();
			updateAvailable = newer(latest, current());
			status = updateAvailable ? "Update available: " + latest + " (you have " + current() + ")" : "You are up to date (" + current() + ")";
		} catch (Exception e) {
			status = "Update check unavailable";
		}
	}

	private static boolean newer(String a, String b) {
		int[] x = parse(a), y = parse(b);
		for (int i = 0; i < 3; i++) if (x[i] != y[i]) return x[i] > y[i];
		return false;
	}
	private static int[] parse(String v) {
		int[] out = new int[3];
		String[] p = v.replaceFirst("^[vV]", "").split("[.\\-+]");
		for (int i = 0; i < 3 && i < p.length; i++) {
			try { out[i] = Integer.parseInt(p[i]); } catch (NumberFormatException ignored) {}
		}
		return out;
	}
}
