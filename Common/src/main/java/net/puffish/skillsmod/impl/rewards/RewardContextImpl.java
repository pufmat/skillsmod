package net.puffish.skillsmod.impl.rewards;

import net.puffish.skillsmod.api.rewards.RewardContext;

public record RewardContextImpl(int count, boolean recent) implements RewardContext {

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public boolean isRecent() {
		return recent;
	}
}
