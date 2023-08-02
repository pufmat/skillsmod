package net.puffish.skillsmod.experience.calculation.parameter;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.json.JsonPath;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public interface Parameter<T> extends Function<T, Double> {

	default <R> Parameter<R> map(Function<R, T> function) {
		return r -> this.apply(function.apply(r));
	}

	static <T> Result<Parameter<T>, Failure> parse(JsonElementWrapper rootElement, Map<String, ParameterFactory<T>> factories, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, factories, context));
	}

	static <T> Result<Parameter<T>, Failure> parse(JsonObjectWrapper rootObject, Map<String, ParameterFactory<T>> factories, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optType = rootObject.getString("type")
				.ifFailure(failures::add)
				.getSuccess();

		var maybeDataElement = rootObject.get("data");

		var optFallback = rootObject.get("fallback")
				.getSuccess()
				.flatMap(fallbackElement -> fallbackElement.getAsDouble()
						.ifFailure(failures::add)
						.getSuccess()
				);

		if (failures.isEmpty()) {
			return build(
					optType.orElseThrow(),
					maybeDataElement,
					rootObject.getPath().thenObject("type"),
					factories,
					context
			).orElse(failure -> {
				if (optFallback.isPresent()) {
					context.addWarning(failure);
					return Result.success(new FallbackParameter<>(optFallback.orElseThrow()));
				} else {
					return Result.failure(failure);
				}
			});
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	private static <T> Result<Parameter<T>, Failure> build(String type, Result<JsonElementWrapper, Failure> maybeDataElement, JsonPath typePath, Map<String, ParameterFactory<T>> factories, ConfigContext context) {
		var factory = factories.get(type);
		if (factory == null) {
			return Result.failure(typePath.failureAt("Expected a valid parameter type"));
		} else {
			return factory.apply(maybeDataElement, context).mapSuccess(c -> (Parameter<T>) c);
		}
	}
}
