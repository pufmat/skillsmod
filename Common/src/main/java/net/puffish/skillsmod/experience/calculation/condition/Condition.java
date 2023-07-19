package net.puffish.skillsmod.experience.calculation.condition;

import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.json.JsonPath;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Condition<T> extends Predicate<T> {

	default <R> Condition<R> map(Function<R, T> function) {
		return r -> this.test(function.apply(r));
	}

	static <T> Result<Condition<T>, Error> parse(JsonElementWrapper rootElement, Map<String, ConditionFactory<T>> factories) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, factories));
	}

	static <T> Result<Condition<T>, Error> parse(JsonObjectWrapper rootObject, Map<String, ConditionFactory<T>> factories) {
		var errors = new ArrayList<Error>();

		var optType = rootObject.getString("type")
				.ifFailure(errors::add)
				.getSuccess();

		var maybeDataElement = rootObject.get("data");

		if (errors.isEmpty()) {
			return build(
					optType.orElseThrow(),
					maybeDataElement,
					rootObject.getPath().thenObject("type"),
					factories
			);
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	private static <T> Result<Condition<T>, Error> build(String type, Result<JsonElementWrapper, Error> maybeDataElement, JsonPath typePath, Map<String, ConditionFactory<T>> factories) {
		var factory = factories.get(type);
		if (factory == null) {
			return  Result.failure(typePath.errorAt("Expected a valid condition type"));
		} else {
			return factory.apply(maybeDataElement).mapSuccess(c -> (Condition<T>) c);
		}
	}
}
