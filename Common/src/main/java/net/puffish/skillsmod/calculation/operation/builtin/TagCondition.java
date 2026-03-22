package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.world.entity.Entity;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.calculation.LegacyBuiltinPrototypes;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public final class TagCondition implements Operation<Entity, Boolean> {
	private final String tag;

	private TagCondition(String tag) {
		this.tag = tag;
	}

	public static void register() {
		BuiltinPrototypes.ENTITY.registerOperation(
				SkillsMod.createIdentifier("has_tag"),
				BuiltinPrototypes.BOOLEAN,
				TagCondition::parse
		);

		LegacyBuiltinPrototypes.registerAlias(
				BuiltinPrototypes.ENTITY,
				SkillsMod.createIdentifier("tag"),
				SkillsMod.createIdentifier("has_tag")
		);
	}

	public static Result<TagCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(TagCondition::parse, context));
	}

	public static Result<TagCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optTag = rootObject.getString("tag")
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new TagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(Entity entity) {
		return Optional.of(entity.getTags().contains(tag));
	}
}
