package net.puffish.skillsmod.api.experience;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;

public interface ExperienceSourceWithDataFactory {
	Result<? extends ExperienceSource, Failure> create(JsonElementWrapper json, ConfigContext context);
}
