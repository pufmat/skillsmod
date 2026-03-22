package net.puffish.skillsmod.client.keybinding;

import net.minecraft.client.KeyMapping;

public interface KeyBindingReceiver {
	void registerKeyBinding(KeyMapping keyBinding, KeyBindingHandler handler);
}
