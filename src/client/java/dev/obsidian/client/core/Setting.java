package dev.obsidian.client.core;

/** One user-adjustable value: on/off, number slider or a list of choices. Stored as a double. */
public class Setting {
	public enum Type { BOOL, NUM, MODE }

	public final String id, name;
	public final Type type;
	public double value;
	public final double min, max, step;
	public final String[] modes;

	private Setting(String id, String name, Type type, double def, double min, double max, double step, String[] modes) {
		this.id = id; this.name = name; this.type = type;
		this.value = def; this.min = min; this.max = max; this.step = step; this.modes = modes;
	}

	public static Setting bool(String id, String name, boolean def) {
		return new Setting(id, name, Type.BOOL, def ? 1 : 0, 0, 1, 1, null);
	}
	public static Setting num(String id, String name, double def, double min, double max, double step) {
		return new Setting(id, name, Type.NUM, def, min, max, step, null);
	}
	public static Setting mode(String id, String name, String... modes) {
		return new Setting(id, name, Type.MODE, 0, 0, modes.length - 1, 1, modes);
	}

	public boolean on() { return value >= 0.5; }
	public double num() { return value; }
	public int intValue() { return (int) Math.round(value); }
	public String modeName() { return modes[Math.max(0, Math.min(modes.length - 1, intValue()))]; }

	public void set(double v) {
		v = Math.max(min, Math.min(max, v));
		if (step > 0) v = Math.round((v - min) / step) * step + min;
		value = Math.max(min, Math.min(max, v));
	}
	public void toggle() { value = on() ? 0 : 1; }
	public void cycle(int dir) {
		int n = modes.length;
		value = ((intValue() + dir) % n + n) % n;
	}
	public String display() {
		return switch (type) {
			case BOOL -> on() ? "ON" : "OFF";
			case MODE -> modeName();
			case NUM -> step >= 1 ? Integer.toString(intValue()) : String.format("%.2f", value);
		};
	}
}
