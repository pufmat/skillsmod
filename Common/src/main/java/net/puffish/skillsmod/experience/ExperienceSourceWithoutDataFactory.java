package net.puffish.skillsmod.experience;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

public interface ExperienceSourceWithoutDataFactory {
	Result<? extends ExperienceSource, Error> create(ConfigContext context);
}
