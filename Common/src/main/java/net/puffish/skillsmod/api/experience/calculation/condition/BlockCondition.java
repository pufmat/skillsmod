package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.StatePredicate;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

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

	public static Result<BlockCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(BlockCondition::parse);
	}

	public static Result<BlockCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optBlock = rootObject.get("block")
				.andThen(JsonParseUtils::parseBlock)
				.ifFailure(failures::add)
				.getSuccess();

		var state = rootObject.get("state")
				.getSuccess()
				.flatMap(stateElement -> JsonParseUtils.parseStatePredicate(stateElement)
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElseGet(() -> StatePredicate.Builder.create().build());

		if (failures.isEmpty()) {
			return Result.success(new BlockCondition(
					optBlock.orElseThrow(),
					state
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public boolean test(BlockState blockState) {
		return blockState.isOf(block) && state.test(blockState);
	}
}
