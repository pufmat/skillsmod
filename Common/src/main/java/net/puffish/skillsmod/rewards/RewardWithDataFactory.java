package net.puffish.skillsmod.rewards;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

public interface RewardWithDataFactory {
	Result<? extends Reward, Error> create(JsonElementWrapper json, ConfigContext context);
}
