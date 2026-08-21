package net.puffish.skillsmod.config;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.function.Function;

public record ExchangeConfig(
		Function<Integer, Integer> function,
		int levelLimit
) {

	public static Result<ExchangeConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(rootObject -> rootObject.noUnused(o -> parse(o, context)));
	}

	public static Result<ExchangeConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var levelLimit = rootObject.get("level_limit")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(Integer.MAX_VALUE);

		var optCostPerLevel = rootObject.get("cost_per_level")
				.andThen(element -> CurveConfig.parse(element, context))
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new ExchangeConfig(
					optCostPerLevel.orElseThrow().function(),
					levelLimit
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}
