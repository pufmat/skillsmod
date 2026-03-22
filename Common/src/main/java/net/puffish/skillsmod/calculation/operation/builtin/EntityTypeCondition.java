package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EntityType;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public final class EntityTypeCondition implements Operation<EntityType<?>, Boolean> {
	private final HolderSet<EntityType<?>> entityTypeEntries;

	private EntityTypeCondition(HolderSet<EntityType<?>> entityTypeEntries) {
		this.entityTypeEntries = entityTypeEntries;
	}

	public static void register() {
		BuiltinPrototypes.ENTITY_TYPE.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				EntityTypeCondition::parse
		);
	}

	public static Result<EntityTypeCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	public static Result<EntityTypeCondition, Problem> parse(JsonObject rootObject, OperationConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optEntityType = rootObject.get("entity_type")
				.orElse(LegacyUtils.wrapDeprecated(
						() -> rootObject.get("entity"),
						3,
						context
				))
				.andThen(BuiltinJson::parseEntityTypeOrEntityTypeTag)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new EntityTypeCondition(
					optEntityType.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(EntityType<?> entityType) {
		return Optional.of(entityType.is(entityTypeEntries));
	}
}
