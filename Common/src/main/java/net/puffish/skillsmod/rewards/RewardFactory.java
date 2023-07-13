package net.puffish.skillsmod.rewards;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.Result;

public interface RewardFactory {
	Result<? extends Reward, Error> create(Result<JsonElementWrapper, Error> maybeDataElement, ConfigContext context);
}
