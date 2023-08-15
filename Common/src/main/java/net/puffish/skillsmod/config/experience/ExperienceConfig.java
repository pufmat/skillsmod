package net.puffish.skillsmod.config.experience;

import net.minecraft.server.MinecraftServer;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

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

	public static Result<ExperienceConfig, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<ExperienceConfig, Failure> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optEnabled = rootObject.getBoolean("enabled")
				.ifFailure(failures::add)
				.getSuccess();

		var optExperiencePerLevel = rootObject.get("experience_per_level")
				.andThen(ExperiencePerLevelConfig::parse)
				.ifFailure(failures::add)
				.getSuccess();

		var experienceSources = rootObject.getArray("sources")
				.andThen(array -> array.getAsList((i, element) -> ExperienceSourceConfig.parse(element, context)).mapFailure(ManyFailures::ofList))
				.ifFailure(failures::add)
				.getSuccess()
				.orElseGet(List::of);

		if (failures.isEmpty()) {
			return Result.success(new ExperienceConfig(
					optEnabled.orElseThrow(),
					optExperiencePerLevel.orElseThrow(),
					experienceSources
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public int getRequiredExperience(int level) {
		return experiencePerLevel.getFunction().apply(level);
	}

	public int getCurrentExperience(int earnedExperience) {
		int level = 0;

		while (true) {
			int requiredExperience = getRequiredExperience(level);

			if (earnedExperience < requiredExperience) {
				return earnedExperience;
			}

			earnedExperience -= requiredExperience;
			level++;
		}
	}

	public int getCurrentLevel(int earnedExperience) {
		int level = 0;

		while (true) {
			int requiredExperience = getRequiredExperience(level);

			if (earnedExperience < requiredExperience) {
				return level;
			}

			earnedExperience -= requiredExperience;
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
