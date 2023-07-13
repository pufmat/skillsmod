package net.puffish.skillsmod.experience.calculation.parameter;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface ParameterFactory<T> extends BiFunction<Result<JsonElementWrapper, Error>, ConfigContext, Result<? extends Parameter<T>, Error>> {
	default <R> ParameterFactory<R> map(Function<Parameter<T>, ? extends Parameter<R>> function) {
		return (maybeData, context) -> this.apply(maybeData, context).mapSuccess(function::apply);
	}

	static <T> ParameterFactory<T> simple(Function<T, Double> function) {
		return (maybeData, context) -> Result.success(function::apply);
	}

	static <T> ParameterFactory<T> withoutData(Function<ConfigContext, Result<? extends Parameter<T>, Error>> factory) {
		return (maybeData, context) -> factory.apply(context);
	}

	static <T> ParameterFactory<T> withData(BiFunction<JsonElementWrapper, ConfigContext, Result<? extends Parameter<T>, Error>> factory) {
		return (maybeData, context) -> maybeData.andThen(data -> factory.apply(data, context));
	}
}
