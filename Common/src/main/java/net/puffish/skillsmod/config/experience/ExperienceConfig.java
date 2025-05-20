package net.puffish.skillsmod.config.experience;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.experience.ExperienceCurve;
import net.puffish.skillsmod.util.DisposeContext;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ExperienceConfig(
		ExperienceCurve curve,
		List<ExperienceSourceConfig> experienceSources
) {

	public static Result<Optional<ExperienceConfig>, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	public static Result<Optional<ExperienceConfig>, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var enabled = LegacyUtils.deprecated(
				() -> rootObject.getBoolean("enabled"),
				3,
				context
		).orElse(true);

		var levelLimit = rootObject.get("level_limit")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(Integer.MAX_VALUE);

		var optExperiencePerLevel = rootObject.get("experience_per_level")
				.andThen(element -> ExperiencePerLevelConfig.parse(element, context))
				.ifFailure(problems::add)
				.getSuccess();

		var experienceSources = rootObject.getArray("sources")
				.andThen(array -> array.getAsList((i, element) -> ExperienceSourceConfig.parse(element, context)).mapFailure(Problem::combine))
				.ifFailure(problems::add)
				.getSuccess()
				.orElseGet(List::of);

		if (problems.isEmpty()) {
			if (enabled) {
				return Result.success(Optional.of(new ExperienceConfig(
						ExperienceCurve.create(optExperiencePerLevel.orElseThrow().function(), levelLimit),
						experienceSources
				)));
			} else {
				return Result.success(Optional.empty());
			}
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public void dispose(DisposeContext context) {
		for (var experienceSource : experienceSources) {
			experienceSource.dispose(context);
		}
	}

}
