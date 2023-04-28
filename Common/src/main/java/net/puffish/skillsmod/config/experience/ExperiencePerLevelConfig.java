package net.puffish.skillsmod.config.experience;

import net.puffish.skillsmod.expression.ArithmeticParser;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.json.JsonPath;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ExperiencePerLevelConfig {
	private final Function<Integer, Integer> function;

	private ExperiencePerLevelConfig(Function<Integer, Integer> function) {
		this.function = function;
	}

	public static Result<ExperiencePerLevelConfig, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(ExperiencePerLevelConfig::parse);
	}

	public static Result<ExperiencePerLevelConfig, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var maybeDataElement = rootObject.get("data");

		var optFunction = rootObject.get("type")
				.andThen(typeElement -> typeElement.getAsString()
						.andThen(type -> parseType(type, maybeDataElement, typeElement.getPath()))
				)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new ExperiencePerLevelConfig(
					optFunction.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	private static Result<Function<Integer, Integer>, Error> parseType(String type, Result<JsonElementWrapper, Error> maybeDataElement, JsonPath typeElementPath) {
		return switch (type) {
			case "expression" -> parseExpression(maybeDataElement);
			case "values" -> parseValues(maybeDataElement);
			default -> Result.failure(typeElementPath.errorAt("Expected a valid condition type"));
		};
	}

	private static Result<Function<Integer, Integer>, Error> parseExpression(Result<JsonElementWrapper, Error> maybeDataElement) {
		return maybeDataElement
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(dataObject -> dataObject.getString("expression"))
				.andThen(expression -> ArithmeticParser.parse(expression, Set.of("level")))
				.mapSuccess(expression -> level -> {
					var value = expression.eval(Map.ofEntries(Map.entry("level", (double) level)));
					if (Double.isFinite(value)) {
						return (int) Math.round(value);
					} else {
						return 0;
					}
				});
	}

	private static Result<Function<Integer, Integer>, Error> parseValues(Result<JsonElementWrapper, Error> maybeDataElement) {
		return maybeDataElement
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(dataObject -> dataObject.getArray("values"))
				.andThen(valueArray -> valueArray.getAsList((k, element) -> element.getAsInt()).mapFailure(ManyErrors::ofList))
				.mapSuccess(values -> level -> values.get(Math.min(level, values.size() - 1)));
	}

	public Function<Integer, Integer> getFunction() {
		return function;
	}
}
