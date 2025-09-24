package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public record SkillConfig(
		String id,
		int x,
		int y,
		String definitionId,
		boolean isRoot
) {

	public static Result<Optional<SkillConfig>, Problem> parse(String id, JsonElement rootElement, SkillDefinitionsConfig definitions, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(id, rootObject, definitions), context)
		);
	}

	private static Result<Optional<SkillConfig>, Problem> parse(String id, JsonObject rootObject, SkillDefinitionsConfig definitions) {
		var problems = new ArrayList<Problem>();

		var optX = rootObject.getInt("x")
				.ifFailure(problems::add)
				.getSuccess();

		var optY = rootObject.getInt("y")
				.ifFailure(problems::add)
				.getSuccess();

		var optDefinitionId = rootObject.get("definition")
				.andThen(definitionElement -> definitionElement.getAsString()
						.andThen(definitionId -> {
							if (definitions.isValid(definitionId)) {
								return Result.success(definitionId);
							} else {
								return Result.failure(definitionElement.getPath().createProblem("Expected a valid definition"));
							}
						})
				)
				.ifFailure(problems::add)
				.getSuccess();

		var isRoot = rootObject.get("root")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsBoolean()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(false);

		if (problems.isEmpty()) {
			var definitionId = optDefinitionId.orElseThrow();
			if (definitions.isLoaded(definitionId)) {
				return Result.success(Optional.of(new SkillConfig(
						id,
						optX.orElseThrow(),
						optY.orElseThrow(),
						definitionId,
						isRoot
				)));
			} else {
				return Result.success(Optional.empty());
			}
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}
