package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.registry.RegistryEntryList;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class BlockTagCondition implements Condition<BlockState> {
	private final RegistryEntryList.Named<Block> entries;

	private BlockTagCondition(RegistryEntryList.Named<Block> entries) {
		this.entries = entries;
	}

	public static ConditionFactory<BlockState> factory() {
		return ConditionFactory.withData(BlockTagCondition::parse);
	}

	public static Result<BlockTagCondition, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(BlockTagCondition::parse);
	}

	public static Result<BlockTagCondition, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optTag = rootObject.get("tag")
				.andThen(JsonParseUtils::parseBlockTag)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new BlockTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public boolean test(BlockState blockState) {
		return blockState.isIn(entries);
	}
}
