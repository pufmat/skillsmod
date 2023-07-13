package net.puffish.skillsmod.experience;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

public interface ExperienceSourceFactory {
	Result<? extends ExperienceSource, Error> create(Result<JsonElementWrapper, Error> maybeDataElement, ConfigContext context);
}
