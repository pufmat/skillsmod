package net.puffish.skillsmod.config.experience;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.expression.ArithmeticParser;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ExperiencePerLevelConfig {
	private final Function<Integer, Integer> function;

	private ExperiencePerLevelConfig(Function<Integer, Integer> function) {
		this.function = function;
	}

	public static Result<ExperiencePerLevelConfig, Failure> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(ExperiencePerLevelConfig::parse);
	}

	public static Result<ExperiencePerLevelConfig, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var maybeDataElement = rootObject.get("data");

		var optFunction = rootObject.get("type")
				.andThen(typeElement -> typeElement.getAsString()
						.andThen(type -> parseType(type, maybeDataElement, typeElement.getPath()))
				)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new ExperiencePerLevelConfig(
					optFunction.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	private static Result<Function<Integer, Integer>, Failure> parseType(String type, Result<JsonElementWrapper, Failure> maybeDataElement, JsonPath typeElementPath) {
		return switch (type) {
			case "expression" -> parseExpression(maybeDataElement);
			case "values" -> parseValues(maybeDataElement);
			default -> Result.failure(typeElementPath.failureAt("Expected a valid condition type"));
		};
	}

	private static Result<Function<Integer, Integer>, Failure> parseExpression(Result<JsonElementWrapper, Failure> maybeDataElement) {
		return maybeDataElement
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(dataObject -> dataObject.get("expression"))
				.andThen(expressionElement -> expressionElement.getAsString()
						.andThen(expression -> ArithmeticParser.parse(expression, Set.of("level")))
						.mapSuccess(expression -> level -> {
							var value = expression.eval(Map.ofEntries(Map.entry("level", (double) level)));
							if (Double.isFinite(value)) {
								return (int) Math.round(value);
							} else {
								for (var message : expressionElement.getPath().failureAt("Expression returned a value that is not finite").getMessages()) {
									SkillsMod.getInstance().getLogger().warn(message);
								}
								return 0;
							}
						})
				);
	}

	private static Result<Function<Integer, Integer>, Failure> parseValues(Result<JsonElementWrapper, Failure> maybeDataElement) {
		return maybeDataElement
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(dataObject -> dataObject.getArray("values"))
				.andThen(valueArray -> valueArray.getAsList((k, element) -> element.getAsInt()).mapFailure(ManyFailures::ofList))
				.mapSuccess(values -> level -> values.get(Math.min(level, values.size() - 1)));
	}

	public Function<Integer, Integer> getFunction() {
		return function;
	}
}
