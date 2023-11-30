package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.entity.EntityType;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;

public final class EntityTypeCondition implements Condition<EntityType<?>> {
	private final EntityType<?> entityType;

	private EntityTypeCondition(EntityType<?> entityType) {
		this.entityType = entityType;
	}

	public static ConditionFactory<EntityType<?>> factory() {
		return ConditionFactory.withData(EntityTypeCondition::parse);
	}

	public static Result<EntityTypeCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(EntityTypeCondition::parse);
	}

	public static Result<EntityTypeCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optEntity = rootObject.get("entity")
				.andThen(JsonParseUtils::parseEntityType)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new EntityTypeCondition(
					optEntity.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public boolean test(EntityType<?> entityType) {
		return this.entityType == entityType;
	}
}
