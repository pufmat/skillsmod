package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.registry.entry.RegistryEntryList;
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

public final class EntityCondition implements Operation<Entity, Boolean> {
	private final Optional<RegistryEntryList<EntityType<?>>> optEntityTypeEntries;
	private final Optional<NbtPredicate> optNbt;

	private EntityCondition(Optional<RegistryEntryList<EntityType<?>>> optEntityTypeEntries, Optional<NbtPredicate> optNbt) {
		this.optEntityTypeEntries = optEntityTypeEntries;
		this.optNbt = optNbt;
	}

	public static void register() {
		BuiltinPrototypes.ENTITY.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				EntityCondition::parse
		);
	}

	public static Result<EntityCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> rootObject.noUnused(EntityCondition::parse));
	}

	public static Result<EntityCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optEntityType = rootObject.get("entity_type")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(entityElement -> BuiltinJson.parseEntityTypeOrEntityTypeTag(entityElement)
						.ifFailure(problems::add)
						.getSuccess()
				);

		var optNbt = rootObject.get("nbt")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(nbtElement -> BuiltinJson.parseNbtPredicate(nbtElement)
						.ifFailure(problems::add)
						.getSuccess()
				);

		if (problems.isEmpty()) {
			return Result.success(new EntityCondition(
					optEntityType,
					optNbt
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(Entity entity) {
		return Optional.of(
				optEntityTypeEntries.map(entityTypeEntries -> entity.getType().isIn(entityTypeEntries)).orElse(true)
						&& optNbt.map(nbt -> nbt.test(entity)).orElse(true)
		);
	}
}
