package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;

public interface Skill {
	Category getCategory();

	String getId();

	boolean isUnlocked(ServerPlayerEntity player);

	void unlock(ServerPlayerEntity player);
}
