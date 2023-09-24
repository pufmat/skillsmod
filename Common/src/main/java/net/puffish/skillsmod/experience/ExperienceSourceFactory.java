package net.puffish.skillsmod.experience;

import net.puffish.skillsmod.api.experience.ExperienceSource;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;

public interface ExperienceSourceFactory {
	Result<? extends ExperienceSource, Failure> create(Result<JsonElementWrapper, Failure> maybeDataElement, ConfigContext context);
}
