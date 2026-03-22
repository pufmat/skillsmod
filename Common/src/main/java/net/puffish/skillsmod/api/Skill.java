package net.puffish.skillsmod.api;

import net.minecraft.server.level.ServerPlayer;

public interface Skill {
	Category getCategory();

	String getId();

	State getState(ServerPlayer player);

	void unlock(ServerPlayer player);

	void lock(ServerPlayer player);

	enum State {
		LOCKED,
		AVAILABLE,
		AFFORDABLE,
		UNLOCKED,
		EXCLUDED
	}
}
