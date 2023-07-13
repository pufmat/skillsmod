package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.block.BlockState;
import net.minecraft.predicate.StatePredicate;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class BlockStateCondition implements Condition<BlockState> {
	private final StatePredicate state;

	public BlockStateCondition(StatePredicate state) {
		this.state = state;
	}

	public static ConditionFactory<BlockState> factory() {
		return ConditionFactory.withData(BlockStateCondition::parse);
	}

	public static Result<BlockStateCondition, Error> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(BlockStateCondition::parse);
	}

	public static Result<BlockStateCondition, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optState = rootObject.get("state")
				.andThen(JsonParseUtils::parseStatePredicate)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new BlockStateCondition(
					optState.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public boolean test(BlockState blockState) {
		return state.test(blockState);
	}
}
