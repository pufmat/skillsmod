package net.puffish.skillsmod.rewards;

import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

public interface RewardWithoutDataFactory {
	Result<? extends Reward, Error> create();
}
