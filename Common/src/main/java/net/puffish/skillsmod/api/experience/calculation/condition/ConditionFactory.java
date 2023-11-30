package net.puffish.skillsmod.api.experience.calculation.condition;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ConditionFactory<T> extends BiFunction<Result<JsonElementWrapper, Failure>, ConfigContext, Result<? extends Condition<T>, Failure>> {
	default <R> ConditionFactory<R> map(Function<Condition<T>, ? extends Condition<R>> function) {
		return (maybeData, context) -> this.apply(maybeData, context).mapSuccess(function::apply);
	}

	static <T> ConditionFactory<T> simple(Predicate<T> predicate) {
		return (maybeData, context) -> Result.success(predicate::test);
	}

	static <T> ConditionFactory<T> withoutData(Function<ConfigContext, Result<? extends Condition<T>, Failure>> factory) {
		return (maybeData, context) -> factory.apply(context);
	}

	static <T> ConditionFactory<T> withData(BiFunction<JsonElementWrapper, ConfigContext, Result<? extends Condition<T>, Failure>> factory) {
		return (maybeData, context) -> maybeData.andThen(data -> factory.apply(data, context));
	}
}
