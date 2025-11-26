package net.puffish.skillsmod.experience.source.builtin.util;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

public enum TamedActivity {
	EXCLUDE,
	INCLUDE,
	ONLY;

	public static Result<TamedActivity, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsString().andThen(string -> switch (string) {
			case "exclude" -> Result.success(TamedActivity.EXCLUDE);
			case "include" -> Result.success(TamedActivity.INCLUDE);
			case "only" -> Result.success(TamedActivity.ONLY);
			default -> Result.failure(rootElement.getPath().createProblem("Expected a valid tamed option"));
		});
	}
}
