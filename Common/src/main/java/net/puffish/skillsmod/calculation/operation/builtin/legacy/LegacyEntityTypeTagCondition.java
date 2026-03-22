package net.puffish.skillsmod.calculation.operation.builtin.legacy;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.ArrayList;
import java.util.Optional;

public final class LegacyEntityTypeTagCondition implements Operation<EntityType<?>, Boolean> {
	private final HolderSet<EntityType<?>> entries;

	private LegacyEntityTypeTagCondition(HolderSet<EntityType<?>> entries) {
		this.entries = entries;
	}

	public static void register() {
		BuiltinPrototypes.ENTITY_TYPE.registerOperation(
				SkillsMod.createIdentifier("legacy_entity_type_tag"),
				BuiltinPrototypes.BOOLEAN,
				LegacyEntityTypeTagCondition::parse
		);
	}

	public static Result<LegacyEntityTypeTagCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyEntityTypeTagCondition::parse);
	}

	public static Result<LegacyEntityTypeTagCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optTag = rootObject.get("tag")
				.andThen(BuiltinJson::parseEntityTypeTag)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new LegacyEntityTypeTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(EntityType<?> entityType) {
		return Optional.of(entries.contains(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entityType)));
	}
}
