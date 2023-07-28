package net.puffish.skillsmod.rewards;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;

public interface RewardFactory {
	Result<? extends Reward, Failure> create(Result<JsonElementWrapper, Failure> maybeDataElement, ConfigContext context);
}
