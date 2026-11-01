package dev.obsidian.client.module;

/** A module that is just an on/off switch other code checks (e.g. the custom main menu). */
public class FlagModule extends Module {
	public FlagModule(String id, String name, String description, Category cat) {
		super(id, name, description, cat, 0, 0);
	}
}
