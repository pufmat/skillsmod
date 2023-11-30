package net.puffish.skillsmod.api.experience.calculation.parameter;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface ParameterFactory<T> extends BiFunction<Result<JsonElementWrapper, Failure>, ConfigContext, Result<? extends Parameter<T>, Failure>> {
	default <R> ParameterFactory<R> map(Function<Parameter<T>, ? extends Parameter<R>> function) {
		return (maybeData, context) -> this.apply(maybeData, context).mapSuccess(function::apply);
	}

	static <T> ParameterFactory<T> simple(Function<T, Double> function) {
		return (maybeData, context) -> Result.success(function::apply);
	}

	static <T> ParameterFactory<T> withoutData(Function<ConfigContext, Result<? extends Parameter<T>, Failure>> factory) {
		return (maybeData, context) -> factory.apply(context);
	}

	static <T> ParameterFactory<T> withData(BiFunction<JsonElementWrapper, ConfigContext, Result<? extends Parameter<T>, Failure>> factory) {
		return (maybeData, context) -> maybeData.andThen(data -> factory.apply(data, context));
	}
}
