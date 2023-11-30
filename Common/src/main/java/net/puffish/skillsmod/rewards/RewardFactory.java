package net.puffish.skillsmod.rewards;

import net.puffish.skillsmod.api.rewards.Reward;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

public interface RewardFactory {
	Result<? extends Reward, Failure> create(Result<JsonElementWrapper, Failure> maybeDataElement, ConfigContext context);
}
