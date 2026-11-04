package dev.obsidian.client.core;

/** Client roles. Each has a logo version (a glyph in the Obsidian font) and a default colour. */
public enum Role {
	MEMBER("member", "Member", 0xA78BFA, ''),
	MEDIA("media", "Media", 0xEF4444, ''),
	STAFF("staff", "Staff", 0x3B82F6, ''),
	HIGH_STAFF("high_staff", "High Staff", 0xF97316, ''),
	OWNER("owner", "Owner", 0xFBBF24, '');

	public final String id, label;
	public final int color;
	public final char glyph;

	Role(String id, String label, int color, char glyph) {
		this.id = id; this.label = label; this.color = color; this.glyph = glyph;
	}

	public static Role of(String id) {
		for (Role r : values()) if (r.id.equals(id)) return r;
		return MEMBER;
	}
}
