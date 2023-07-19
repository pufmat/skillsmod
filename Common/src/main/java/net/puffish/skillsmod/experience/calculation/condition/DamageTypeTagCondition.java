package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class DamageTypeTagCondition implements Condition<RegistryEntry<DamageType>> {
	private final RegistryEntryList.Named<DamageType> entries;

	private DamageTypeTagCondition(RegistryEntryList.Named<DamageType> entries) {
		this.entries = entries;
	}

	public static ConditionFactory<RegistryEntry<DamageType>> factory() {
		return ConditionFactory.withData(DamageTypeTagCondition::parse);
	}

	public static Result<DamageTypeTagCondition, Error> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<DamageTypeTagCondition, Error> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var errors = new ArrayList<Error>();

		var optTag = rootObject.get("tag")
				.andThen(element -> JsonParseUtils.parseDamageTypeTag(element, context.dynamicRegistryManager()))
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new DamageTypeTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public boolean test(RegistryEntry<DamageType> damageType) {
		return entries.contains(damageType);
	}
}
