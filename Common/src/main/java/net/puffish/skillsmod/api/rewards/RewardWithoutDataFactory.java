package net.puffish.skillsmod.api.rewards;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;

public interface RewardWithoutDataFactory {
	Result<? extends Reward, Failure> create(ConfigContext context);
}
