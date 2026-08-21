package net.puffish.skillsmod.config.colors;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;

public record ExchangeCostColorsConfig(
		ColorConfig available,
		ColorConfig affordable,
		ColorConfig hovered
) {
	private static final ColorConfig DEFAULT_AVAILABLE = new ColorConfig(0xff407f10);
	private static final ColorConfig DEFAULT_AFFORDABLE_HOVERED = new ColorConfig(0xff80ff20);

	public static ExchangeCostColorsConfig createDefault() {
		return new ExchangeCostColorsConfig(
				DEFAULT_AVAILABLE,
				DEFAULT_AFFORDABLE_HOVERED,
				DEFAULT_AFFORDABLE_HOVERED
		);
	}

	public static Result<ExchangeCostColorsConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context)
		);
	}

	private static Result<ExchangeCostColorsConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var available = rootObject.get("available")
				.getSuccess()
				.flatMap(element -> ColorConfig.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_AVAILABLE);

		var affordable = rootObject.get("affordable")
				.getSuccess()
				.flatMap(element -> ColorConfig.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_AFFORDABLE_HOVERED);

		var hovered = rootObject.get("hovered")
				.getSuccess()
				.flatMap(element -> ColorConfig.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_AFFORDABLE_HOVERED);

		if (problems.isEmpty()) {
			return Result.success(new ExchangeCostColorsConfig(
					available,
					affordable,
					hovered
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}