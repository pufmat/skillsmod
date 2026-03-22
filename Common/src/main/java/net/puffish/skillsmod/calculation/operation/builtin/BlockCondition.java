package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
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

public final class BlockCondition implements Operation<Block, Boolean> {
	private final HolderSet<Block> blockEntries;

	private BlockCondition(HolderSet<Block> blockEntries) {
		this.blockEntries = blockEntries;
	}

	public static void register() {
		BuiltinPrototypes.BLOCK.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				BlockCondition::parse
		);
	}

	public static Result<BlockCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(BlockCondition::parse, context));
	}

	public static Result<BlockCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optBlock = rootObject.get("block")
				.andThen(BuiltinJson::parseBlockOrBlockTag)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new BlockCondition(
					optBlock.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(Block block) {
		return Optional.of(blockEntries.contains(BuiltInRegistries.BLOCK.wrapAsHolder(block)));
	}
}
