package net.puffish.skillsmod.reward.builtin;

import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;

public class DummyReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("dummy");

	@Override
	public void update(RewardUpdateContext context) { }

	@Override
	public void dispose(RewardDisposeContext context) { }
}
