package net.puffish.skillsmod.impl.rewards;

import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;

public record RewardUpdateContextImpl(ServerPlayerEntity player, int count, boolean isAction) implements RewardUpdateContext {

	@Override
	public ServerPlayerEntity getPlayer() {
		return player;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public boolean isAction() {
		return isAction;
	}

}
