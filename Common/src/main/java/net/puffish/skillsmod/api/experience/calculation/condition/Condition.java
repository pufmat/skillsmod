package net.puffish.skillsmod.api.experience.calculation.condition;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.experience.calculation.condition.FallbackCondition;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Condition<T> extends Predicate<T> {

	default <R> Condition<R> map(Function<R, T> function) {
		return r -> this.test(function.apply(r));
	}

	static <T> Result<Condition<T>, Failure> parse(JsonElementWrapper rootElement, Map<String, ConditionFactory<T>> factories, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, factories, context));
	}

	static <T> Result<Condition<T>, Failure> parse(JsonObjectWrapper rootObject, Map<String, ConditionFactory<T>> factories, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optType = rootObject.getString("type")
				.ifFailure(failures::add)
				.getSuccess();

		var maybeDataElement = rootObject.get("data");

		var optFallback = rootObject.get("fallback")
				.getSuccess()
				.flatMap(fallbackElement -> fallbackElement.getAsBoolean()
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
					failure.getMessages().forEach(context::addWarning);
					return Result.success(new FallbackCondition<>(optFallback.orElseThrow()));
				} else {
					return Result.failure(failure);
				}
			});
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	private static <T> Result<Condition<T>, Failure> build(String type, Result<JsonElementWrapper, Failure> maybeDataElement, JsonPath typePath, Map<String, ConditionFactory<T>> factories, ConfigContext context) {
		var factory = factories.get(type);
		if (factory == null) {
			return Result.failure(typePath.createFailure("Expected a valid condition type"));
		} else {
			return factory.apply(maybeDataElement, context).mapSuccess(c -> (Condition<T>) c);
		}
	}
}
