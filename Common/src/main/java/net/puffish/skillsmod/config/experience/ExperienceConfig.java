package net.puffish.skillsmod.config.experience;

import net.minecraft.server.MinecraftServer;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExperienceConfig {
	private final ExperiencePerLevelConfig experiencePerLevel;
	private final List<ExperienceSourceConfig> experienceSources;

	private ExperienceConfig(ExperiencePerLevelConfig experiencePerLevel, List<ExperienceSourceConfig> experienceSources) {
		this.experiencePerLevel = experiencePerLevel;
		this.experienceSources = experienceSources;
	}

	public static Result<Optional<ExperienceConfig>, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<Optional<ExperienceConfig>, Failure> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		// Deprecated
		var enabled = rootObject.getBoolean("enabled")
				.getSuccess()
				.orElse(true);

		var optExperiencePerLevel = rootObject.get("experience_per_level")
				.andThen(ExperiencePerLevelConfig::parse)
				.ifFailure(failures::add)
				.getSuccess();

		var experienceSources = rootObject.getArray("sources")
				.andThen(array -> array.getAsList((i, element) -> ExperienceSourceConfig.parse(element, context)).mapFailure(Failure::fromMany))
				.ifFailure(failures::add)
				.getSuccess()
				.orElseGet(List::of);

		if (failures.isEmpty()) {
			if (enabled) {
				return Result.success(Optional.of(new ExperienceConfig(
						optExperiencePerLevel.orElseThrow(),
						experienceSources
				)));
			} else {
				return Result.success(Optional.empty());
			}
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	public int getRequiredExperience(int level) {
		return experiencePerLevel.getFunction().apply(level);
	}

	public int getRequiredTotalExperience(int level) {
		int total = 0;
		for (var i = 0; i < level; i++) {
			total += getRequiredExperience(level);
		}
		return total;
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

	public ExperiencePerLevelConfig getExperiencePerLevel() {
		return experiencePerLevel;
	}

	public List<ExperienceSourceConfig> getExperienceSources() {
		return experienceSources;
	}
}
