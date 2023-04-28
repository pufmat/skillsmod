package net.puffish.skillsmod.experience.calculation.parameter;

import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

import java.util.function.Function;

public interface ParameterFactory<T> extends Function<Result<JsonElementWrapper, Error>, Result<? extends Parameter<T>, Error>> {
	static <T> ParameterFactory<T> simple(Function<T, Double> function) {
		return maybe -> Result.success(function::apply);
	}

	static <T, R> ParameterFactory<R> map(ParameterFactory<T> factory, Function<R, T> function) {
		return maybe -> Parameter.map(maybe, factory, function);
	}
}
