package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.entity.EntityType;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class EntityTypeCondition implements Condition<EntityType<?>> {
	private final EntityType<?> entityType;

	private EntityTypeCondition(EntityType<?> entityType) {
		this.entityType = entityType;
	}

	public static ConditionFactory<EntityType<?>> factory() {
		return ConditionFactory.withData(EntityTypeCondition::parse);
	}

	public static Result<EntityTypeCondition, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(EntityTypeCondition::parse);
	}

	public static Result<EntityTypeCondition, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optEntity = rootObject.get("entity")
				.andThen(JsonParseUtils::parseEntityType)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new EntityTypeCondition(
					optEntity.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public boolean test(EntityType<?> entityType) {
		return this.entityType == entityType;
	}
}
