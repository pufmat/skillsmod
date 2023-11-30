package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;

public final class EntityTypeTagCondition implements Condition<EntityType<?>> {
	private final RegistryEntryList.Named<EntityType<?>> entries;

	private EntityTypeTagCondition(RegistryEntryList.Named<EntityType<?>> entries) {
		this.entries = entries;
	}

	public static ConditionFactory<EntityType<?>> factory() {
		return ConditionFactory.withData(EntityTypeTagCondition::parse);
	}

	public static Result<EntityTypeTagCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(EntityTypeTagCondition::parse);
	}

	public static Result<EntityTypeTagCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optTag = rootObject.get("tag")
				.andThen(JsonParseUtils::parseEntityTypeTag)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new EntityTypeTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public boolean test(EntityType<?> entityType) {
		return entries.contains(Registries.ENTITY_TYPE.getEntry(entityType));
	}
}
