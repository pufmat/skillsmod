package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.StatePredicate;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;
import java.util.Optional;

public final class BlockCondition implements Condition<BlockState> {
	private final Block block;
	private final Optional<StatePredicate> optState;

	private BlockCondition(Block block, Optional<StatePredicate> optState) {
		this.block = block;
		this.optState = optState;
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

		var optState = rootObject.get("state")
				.getSuccess()
				.flatMap(stateElement -> JsonParseUtils.parseStatePredicate(stateElement)
						.ifFailure(failures::add)
						.getSuccess()
				);

		if (failures.isEmpty()) {
			return Result.success(new BlockCondition(
					optBlock.orElseThrow(),
					optState
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public boolean test(BlockState blockState) {
		return blockState.isOf(block) && optState.map(state -> state.test(blockState)).orElse(true);
	}
}
