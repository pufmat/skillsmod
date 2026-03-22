package net.puffish.skillsmod.calculation.operation.builtin.legacy;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class LegacyBlockTagCondition implements Operation<BlockState, Boolean> {
	private final HolderSet<Block> entries;

	private LegacyBlockTagCondition(HolderSet<Block> entries) {
		this.entries = entries;
	}

	public static void register() {
		BuiltinPrototypes.BLOCK_STATE.registerOperation(
				SkillsMod.createIdentifier("legacy_block_tag"),
				BuiltinPrototypes.BOOLEAN,
				LegacyBlockTagCondition::parse
		);
	}

	public static Result<LegacyBlockTagCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyBlockTagCondition::parse);
	}

	public static Result<LegacyBlockTagCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optTag = rootObject.get("tag")
				.andThen(BuiltinJson::parseBlockTag)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new LegacyBlockTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(BlockState blockState) {
		return Optional.of(blockState.is(entries));
	}
}
