package net.puffish.skillsmod.config.experience;

import net.minecraft.server.MinecraftServer;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;
import java.util.List;

public class ExperienceConfig {
	private final boolean enabled;
	private final ExperiencePerLevelConfig experiencePerLevel;
	private final List<ExperienceSourceConfig> experienceSources;

	private ExperienceConfig(boolean enabled, ExperiencePerLevelConfig experiencePerLevel, List<ExperienceSourceConfig> experienceSources) {
		this.enabled = enabled;
		this.experiencePerLevel = experiencePerLevel;
		this.experienceSources = experienceSources;
	}

	public static Result<ExperienceConfig, Error> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<ExperienceConfig, Error> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var errors = new ArrayList<Error>();

		var enabled = rootObject.getBoolean("enabled")
				.ifFailure(errors::add)
				.getSuccess();

		var experiencePerLevel = rootObject.get("experience_per_level")
				.andThen(ExperiencePerLevelConfig::parse)
				.ifFailure(errors::add)
				.getSuccess();

		var experienceSources = rootObject.getArray("sources")
				.andThen(array -> array.getAsList((i, element) -> ExperienceSourceConfig.parse(element, context)).mapFailure(ManyErrors::ofList))
				.ifFailure(errors::add)
				.getSuccess()
				.orElseGet(List::of);

		if (errors.isEmpty()) {
			return Result.success(new ExperienceConfig(
					enabled.orElseThrow(),
					experiencePerLevel.orElseThrow(),
					experienceSources
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	public float getProgress(CategoryData categoryData) {
		int experience = categoryData.getExperience();
		int level = 0;

		while (true) {
			int requiredExperience = experiencePerLevel.getFunction().apply(level);

			if (experience < requiredExperience) {
				return ((float) experience) / ((float) requiredExperience);
			}

			experience -= requiredExperience;
			level++;
		}
	}

	public int getLevel(CategoryData categoryData) {
		int experience = categoryData.getExperience();
		int level = 0;

		while (true) {
			int requiredExperience = experiencePerLevel.getFunction().apply(level);

			if (experience < requiredExperience) {
				return level;
			}

			experience -= requiredExperience;
			level++;
		}
	}

	public void dispose(MinecraftServer server) {
		for (var experienceSource : experienceSources) {
			experienceSource.dispose(server);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public ExperiencePerLevelConfig getExperiencePerLevel() {
		return experiencePerLevel;
	}

	public List<ExperienceSourceConfig> getExperienceSources() {
		return experienceSources;
	}
}
