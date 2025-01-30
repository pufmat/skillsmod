package net.puffish.skillsmod.calculation;

import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.api.calculation.Variables;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.List;

public class LegacyCalculation {
	public static <T> Result<Calculation<T>, Problem> parse(
			JsonElement rootElement,
			Prototype<T> prototype,
			ConfigContext context
	) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, prototype, context), context)
		);
	}

	public static <T> Result<Calculation<T>, Problem> parse(
			JsonObject rootObject,
			Prototype<T> prototype,
			ConfigContext context
	) {
		var problems = new ArrayList<Problem>();

		var variablesList = new ArrayList<Variables<T, Double>>();

		for (var keys : LegacyUtils.isRemoved(3, context)
				? List.of("variables")
				: List.of("parameters", "conditions", "variables")
		) {
			rootObject.get(keys)
					.getSuccess() // ignore failure because this property is optional
					.ifPresent(variablesElement -> Variables.parse(variablesElement, prototype, context)
							.ifFailure(problems::add)
							.ifSuccess(variablesList::add)
					);
		}

		var optCalculation = rootObject.get("experience")
				.andThen(experienceElement -> Calculation.parse(
						experienceElement,
						Variables.combine(variablesList), context)
				)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(optCalculation.orElseThrow());
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}
}
