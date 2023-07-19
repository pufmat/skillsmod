package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.StatePredicate;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class BlockCondition implements Condition<BlockState> {
	private final Block block;
	private final StatePredicate state;

	public BlockCondition(Block block, StatePredicate state) {
		this.block = block;
		this.state = state;
	}

	public static ConditionFactory<BlockState> factory() {
		return ConditionFactory.withData(BlockCondition::parse);
	}

	public static Result<BlockCondition, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(BlockCondition::parse);
	}

	public static Result<BlockCondition, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optBlock = rootObject.get("block")
				.andThen(JsonParseUtils::parseBlock)
				.ifFailure(errors::add)
				.getSuccess();

		var state = rootObject.get("state")
				.getSuccess()
				.flatMap(stateElement -> JsonParseUtils.parseStatePredicate(stateElement)
						.ifFailure(errors::add)
						.getSuccess()
				)
				.orElseGet(() -> StatePredicate.Builder.create().build());

		if (errors.isEmpty()) {
			return Result.success(new BlockCondition(
					optBlock.orElseThrow(),
					state
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public boolean test(BlockState blockState) {
		return blockState.isOf(block) && state.test(blockState);
	}
}
