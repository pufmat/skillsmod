package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

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
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public boolean test(EntityType<?> entityType) {
		return Registry.ENTITY_TYPE.getKey(entityType)
				.map(key -> entries.contains(Registry.ENTITY_TYPE.entryOf(key)))
				.orElse(false);
	}
}
