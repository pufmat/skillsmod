package net.puffish.skillsmod.experience.calculation.condition;

import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ConditionFactory<T> extends Function<Result<JsonElementWrapper, Error>, Result<? extends Condition<T>, Error>> {
	static <T> ConditionFactory<T> simple(Predicate<T> predicate) {
		return maybe -> Result.success(predicate::test);
	}

	static <T, R> ConditionFactory<R> map(ConditionFactory<T> factory, Function<R, T> function) {
		return maybe -> Condition.map(maybe, factory, function);
	}
}
