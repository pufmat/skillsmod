package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

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
				.andThen(element -> JsonParseUtils.parseDamageTypeTag(element, context.getDynamicRegistryManager()))
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new DamageTypeTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public boolean test(RegistryEntry<DamageType> damageType) {
		return entries.contains(damageType);
	}
}
