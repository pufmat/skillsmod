package net.puffish.skillsmod.experience.calculation.condition;

import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ConditionFactory<T> extends Function<Result<JsonElementWrapper, Error>, Result<? extends Condition<T>, Error>> {
	default <R> ConditionFactory<R> map(Function<Condition<T>, ? extends Condition<R>> function) {
		return maybeData -> this.apply(maybeData).mapSuccess(function::apply);
	}

	static <T> ConditionFactory<T> simple(Predicate<T> predicate) {
		return maybeData -> Result.success(predicate::test);
	}

	static <T> ConditionFactory<T> withoutData(Supplier<Result<? extends Condition<T>, Error>> factory) {
		return maybeData -> factory.get();
	}

	static <T> ConditionFactory<T> withData(Function<JsonElementWrapper, Result<? extends Condition<T>, Error>> factory) {
		return maybeData -> maybeData.andThen(factory::apply);
	}
}
