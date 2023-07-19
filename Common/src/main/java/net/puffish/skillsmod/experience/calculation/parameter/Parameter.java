package net.puffish.skillsmod.experience.calculation.parameter;

import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.json.JsonPath;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public interface Parameter<T> extends Function<T, Double> {

	default <R> Parameter<R> map(Function<R, T> function) {
		return r -> this.apply(function.apply(r));
	}

	static <T> Result<Parameter<T>, Error> parse(JsonElementWrapper rootElement, Map<String, ParameterFactory<T>> factories) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, factories));
	}

	static <T> Result<Parameter<T>, Error> parse(JsonObjectWrapper rootObject, Map<String, ParameterFactory<T>> factories) {
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

	private static <T> Result<Parameter<T>, Error> build(String type, Result<JsonElementWrapper, Error> maybeDataElement, JsonPath typePath, Map<String, ParameterFactory<T>> factories) {
		var factory = factories.get(type);
		if (factory == null) {
			return  Result.failure(typePath.errorAt("Expected a valid parameter type"));
		} else {
			return factory.apply(maybeDataElement).mapSuccess(c -> (Parameter<T>) c);
		}
	}
}
