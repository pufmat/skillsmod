package net.puffish.skillsmod.experience;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;

public interface ExperienceSourceWithoutDataFactory {
	Result<? extends ExperienceSource, Failure> create(ConfigContext context);
}
