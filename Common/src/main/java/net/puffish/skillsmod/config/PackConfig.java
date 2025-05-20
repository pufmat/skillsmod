package net.puffish.skillsmod.config;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.List;

public record PackConfig(int version, List<String> categories) implements Config {

	public static Result<PackConfig, Problem> parse(String name, JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnusedConfig(rootObject -> parse(name, rootObject, context), context)
		);
	}

	private static Result<PackConfig, Problem> parse(String name, JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optVersion = rootObject.getInt("version")
				.ifFailure(problems::add)
				.getSuccess();

		var optCategories = rootObject.getArray("categories")
				.andThen(array -> array.getAsList((i, element) -> BuiltinJson.parseIdentifierPath(element))
						.mapFailure(Problem::combine)
				)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			int version = optVersion.orElseThrow();

			if (version < SkillsMod.MIN_CONFIG_VERSION) {
				return Result.failure(Problem.message("Data pack `" + name + "` is outdated. Check out the mod's wiki to learn how to update the data pack."));
			}
			if (version > SkillsMod.MAX_CONFIG_VERSION) {
				return Result.failure(Problem.message("Data pack `" + name + "` is for a newer version of the mod. Please update the mod."));
			}
			if (version < SkillsMod.MAX_CONFIG_VERSION) {
				context.emitWarning(rootObject.getPath()
						.getObject("version")
						.createProblem("Data pack `" + name + "` uses outdated version. Please update the configuration version to " + SkillsMod.MAX_CONFIG_VERSION)
						.toString()
				);
			}

			return Result.success(new PackConfig(
					version,
					optCategories.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}
