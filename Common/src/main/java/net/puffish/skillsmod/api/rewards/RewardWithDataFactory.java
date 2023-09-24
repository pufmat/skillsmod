package net.puffish.skillsmod.api.rewards;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;

public interface RewardWithDataFactory {
	Result<? extends Reward, Failure> create(JsonElementWrapper json, ConfigContext context);
}
