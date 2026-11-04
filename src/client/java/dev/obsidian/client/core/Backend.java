package dev.obsidian.client.core;

/** Where the Obsidian backend lives. Set BASE_URL to your deployed Worker address. */
public final class Backend {
	public static final String BASE_URL = "https://obsidian-backend.obsidianclient.workers.dev";
	public static final String ADMIN_UUID = "f76bed3c-4062-4d8b-ab4a-d7c1f8169202";

	private Backend() {}
	public static boolean configured() { return !BASE_URL.contains("YOUR-SUBDOMAIN"); }
}
