package net.puffish.skillsmod.experience.calculation.condition;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class DamageTypeCondition implements Condition<String> {
	private final String damageType;

	private DamageTypeCondition(String damageType) {
		this.damageType = damageType;
	}

	public static ConditionFactory<String> factory() {
		return ConditionFactory.withData(DamageTypeCondition::parse);
	}

	public static Result<DamageTypeCondition, Error> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(DamageTypeCondition::parse);
	}

	public static Result<DamageTypeCondition, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optDamage = rootObject.getString("damage")
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new DamageTypeCondition(
					optDamage.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public boolean test(String damageType) {
		return this.damageType.equals(damageType);
	}
}
