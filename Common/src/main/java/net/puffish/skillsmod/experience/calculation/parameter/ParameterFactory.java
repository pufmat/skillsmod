package net.puffish.skillsmod.experience.calculation.parameter;

import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ParameterFactory<T> extends Function<Result<JsonElementWrapper, Error>, Result<? extends Parameter<T>, Error>> {
	default <R> ParameterFactory<R> map(Function<Parameter<T>, ? extends Parameter<R>> function) {
		return maybeData -> this.apply(maybeData).mapSuccess(function::apply);
	}

	static <T> ParameterFactory<T> simple(Function<T, Double> function) {
		return maybeData -> Result.success(function::apply);
	}

	static <T> ParameterFactory<T> withoutData(Supplier<Result<? extends Parameter<T>, Error>> factory) {
		return maybeData -> factory.get();
	}

	static <T> ParameterFactory<T> withData(Function<JsonElementWrapper, Result<? extends Parameter<T>, Error>> factory) {
		return maybeData -> maybeData.andThen(factory::apply);
	}
}
