package net.puffish.skillsmod.api.experience.calculation.condition;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;

public final class DamageTypeTagCondition implements Condition<String> {
	private DamageTypeTagCondition() {

	}

	public static ConditionFactory<String> factory() {
		return ConditionFactory.withData(DamageTypeTagCondition::parse);
	}

	public static Result<DamageTypeTagCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<DamageTypeTagCondition, Failure> parse(JsonObjectWrapper ignoredRootObject, ConfigContext ignoredContext) {
		return Result.success(new DamageTypeTagCondition());
	}

	@Override
	public boolean test(String damageType) {
		return true;
	}
}
