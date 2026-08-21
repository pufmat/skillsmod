package net.puffish.skillsmod.config.colors;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;

public record ExchangeColorsConfig(
		ExchangeCostColorsConfig cost
) {
	public static ExchangeColorsConfig createDefault() {
		return new ExchangeColorsConfig(
				ExchangeCostColorsConfig.createDefault()
		);
	}

	public static Result<ExchangeColorsConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context)
		);
	}

	private static Result<ExchangeColorsConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var cost = rootObject.get("cost")
				.getSuccess()
				.flatMap(element -> ExchangeCostColorsConfig.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(ExchangeCostColorsConfig::createDefault);

		if (problems.isEmpty()) {
			return Result.success(new ExchangeColorsConfig(
					cost
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}