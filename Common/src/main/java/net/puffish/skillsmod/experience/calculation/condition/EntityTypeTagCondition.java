package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class EntityTypeTagCondition implements Condition<EntityType<?>> {
	private final RegistryEntryList.Named<EntityType<?>> entries;

	private EntityTypeTagCondition(RegistryEntryList.Named<EntityType<?>> entries) {
		this.entries = entries;
	}

	public static ConditionFactory<EntityType<?>> factory() {
		return ConditionFactory.withData(EntityTypeTagCondition::parse);
	}

	public static Result<EntityTypeTagCondition, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(EntityTypeTagCondition::parse);
	}

	public static Result<EntityTypeTagCondition, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optTag = rootObject.get("tag")
				.andThen(JsonParseUtils::parseEntityTypeTag)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new EntityTypeTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public boolean test(EntityType<?> entityType) {
		return Registry.ENTITY_TYPE.getKey(entityType)
				.map(key -> entries.contains(Registry.ENTITY_TYPE.entryOf(key)))
				.orElse(false);
	}
}
