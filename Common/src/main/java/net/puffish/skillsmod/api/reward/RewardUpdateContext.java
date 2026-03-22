package net.puffish.skillsmod.api.reward;

import net.minecraft.server.level.ServerPlayer;

public interface RewardUpdateContext {
	ServerPlayer getPlayer();

	int getCount();

	boolean isAction();
}
