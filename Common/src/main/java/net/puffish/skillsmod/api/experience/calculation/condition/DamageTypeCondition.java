package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;

public final class DamageTypeCondition implements Condition<RegistryEntry<DamageType>> {
	private final DamageType damageType;

	private DamageTypeCondition(DamageType damageType) {
		this.damageType = damageType;
	}

	public static ConditionFactory<RegistryEntry<DamageType>> factory() {
		return ConditionFactory.withData(DamageTypeCondition::parse);
	}

	public static Result<DamageTypeCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> DamageTypeCondition.parse(rootObject, context));
	}

	public static Result<DamageTypeCondition, Failure> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optDamage = rootObject.get("damage")
				.andThen(damageElement -> JsonParseUtils.parseDamageType(damageElement, context.getDynamicRegistryManager()))
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
	public boolean test(RegistryEntry<DamageType> damageType) {
		return this.damageType == damageType.value();
	}
}
