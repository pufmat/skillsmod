package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class DamageTypeCondition implements Condition<RegistryEntry<DamageType>> {
	private final DamageType damageType;

	private DamageTypeCondition(DamageType damageType) {
		this.damageType = damageType;
	}

	public static ConditionFactory<RegistryEntry<DamageType>> factory() {
		return ConditionFactory.withData(DamageTypeCondition::parse);
	}

	public static Result<DamageTypeCondition, Error> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> DamageTypeCondition.parse(rootObject, context));
	}

	public static Result<DamageTypeCondition, Error> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var errors = new ArrayList<Error>();

		var optDamage = rootObject.get("damage")
				.andThen(damageElement -> JsonParseUtils.parseDamageType(damageElement, context.dynamicRegistryManager()))
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
	public boolean test(RegistryEntry<DamageType> damageType) {
		return this.damageType == damageType.value();
	}
}
