package net.puffish.skillsmod.config.colors;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;

public record ColorsConfig(
		ConnectionsColorsConfig connections,
		ExchangeColorsConfig exchange,
		FillStrokeColorsConfig points
) {
	private static final FillStrokeColorsConfig DEFAULT_POINTS = new FillStrokeColorsConfig(
			new ColorConfig(0xff80ff20),
			new ColorConfig(0xff000000)
	);

	public static ColorsConfig createDefault() {
		return new ColorsConfig(
				ConnectionsColorsConfig.createDefault(),
				ExchangeColorsConfig.createDefault(),
				DEFAULT_POINTS
		);
	}

	public static Result<ColorsConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context)
		);
	}

	private static Result<ColorsConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var connections = rootObject.get("connections")
				.getSuccess()
				.flatMap(element -> ConnectionsColorsConfig.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(ConnectionsColorsConfig::createDefault);

		var exchange = rootObject.get("exchange")
				.getSuccess()
				.flatMap(element -> ExchangeColorsConfig.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(ExchangeColorsConfig::createDefault);

		var points = rootObject.get("points")
				.getSuccess()
				.flatMap(element -> FillStrokeColorsConfig.parse(element, DEFAULT_POINTS, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_POINTS);

		if (problems.isEmpty()) {
			return Result.success(new ColorsConfig(
					connections,
					exchange,
					points
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}
