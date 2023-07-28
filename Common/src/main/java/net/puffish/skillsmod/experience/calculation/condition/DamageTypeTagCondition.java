package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;

public final class DamageTypeTagCondition implements Condition<RegistryEntry<DamageType>> {
	private final RegistryEntryList.Named<DamageType> entries;

	private DamageTypeTagCondition(RegistryEntryList.Named<DamageType> entries) {
		this.entries = entries;
	}

	public static ConditionFactory<RegistryEntry<DamageType>> factory() {
		return ConditionFactory.withData(DamageTypeTagCondition::parse);
	}

	public static Result<DamageTypeTagCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<DamageTypeTagCondition, Failure> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optTag = rootObject.get("tag")
				.andThen(element -> JsonParseUtils.parseDamageTypeTag(element, context.dynamicRegistryManager()))
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new DamageTypeTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public boolean test(RegistryEntry<DamageType> damageType) {
		return entries.contains(damageType);
	}
}
