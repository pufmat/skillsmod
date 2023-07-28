package net.puffish.skillsmod.experience;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;

public interface ExperienceSourceFactory {
	Result<? extends ExperienceSource, Failure> create(Result<JsonElementWrapper, Failure> maybeDataElement, ConfigContext context);
}
