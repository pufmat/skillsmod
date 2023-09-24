package net.puffish.skillsmod.api.experience.calculation.condition;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;

public final class DamageTypeCondition implements Condition<String> {
	private final String damageType;

	private DamageTypeCondition(String damageType) {
		this.damageType = damageType;
	}

	public static ConditionFactory<String> factory() {
		return ConditionFactory.withData(DamageTypeCondition::parse);
	}

	public static Result<DamageTypeCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(DamageTypeCondition::parse);
	}

	public static Result<DamageTypeCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optDamage = rootObject.getString("damage")
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new DamageTypeCondition(
					optDamage.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public boolean test(String damageType) {
		return this.damageType.equals(damageType);
	}
}
