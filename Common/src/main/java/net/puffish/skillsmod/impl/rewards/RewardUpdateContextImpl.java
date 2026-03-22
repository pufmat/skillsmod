package net.puffish.skillsmod.impl.rewards;

import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;

public record RewardUpdateContextImpl(ServerPlayer player, int count, boolean isAction) implements RewardUpdateContext {

	@Override
	public ServerPlayer getPlayer() {
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
