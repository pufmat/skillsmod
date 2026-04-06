package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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

public final class BlockStateCondition implements Operation<BlockState, Boolean> {
	private final Optional<HolderSet<Block>> optBlockEntries;
	private final Optional<StatePropertiesPredicate> optState;

	private BlockStateCondition(Optional<HolderSet<Block>> optBlockEntries, Optional<StatePropertiesPredicate> optState) {
		this.optBlockEntries = optBlockEntries;
		this.optState = optState;
	}

	public static void register() {
		BuiltinPrototypes.BLOCK_STATE.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				BlockStateCondition::parse
		);
	}

	public static Result<BlockStateCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(BlockStateCondition::parse, context));
	}

	public static Result<BlockStateCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optBlock = rootObject.get("block")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(idElement -> BuiltinJson.parseBlockOrBlockTag(idElement)
						.ifFailure(problems::add)
						.getSuccess()
				);

		var optState = rootObject.get("state")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(stateElement -> BuiltinJson.parseStatePropertiesPredicate(stateElement)
						.ifFailure(problems::add)
						.getSuccess()
				);

		if (problems.isEmpty()) {
			return Result.success(new BlockStateCondition(
					optBlock,
					optState
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(BlockState blockState) {
		return Optional.of(
				optBlockEntries.map(blockState::is).orElse(true)
						&& optState.map(state -> state.matches(blockState)).orElse(true)
		);
	}
}
