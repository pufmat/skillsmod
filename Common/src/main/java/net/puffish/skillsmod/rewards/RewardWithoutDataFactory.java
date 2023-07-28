package net.puffish.skillsmod.rewards;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;

public interface RewardWithoutDataFactory {
	Result<? extends Reward, Failure> create(ConfigContext context);
}
