package net.puffish.skillsmod.experience.source.builtin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.experience.source.ExperienceSource;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceDisposeContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.calculation.LegacyBuiltinPrototypes;
import net.puffish.skillsmod.calculation.LegacyCalculation;
import net.puffish.skillsmod.calculation.operation.LegacyOperationRegistry;
import net.puffish.skillsmod.calculation.operation.builtin.AttributeOperation;
import net.puffish.skillsmod.calculation.operation.builtin.BlockStateCondition;
import net.puffish.skillsmod.calculation.operation.builtin.EffectOperation;
import net.puffish.skillsmod.calculation.operation.builtin.ItemStackCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyBlockTagCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyItemTagCondition;

public record MineBlockExperienceSource(
		Calculation<Data> calculation
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("mine_block");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_mined_block_state"),
				BuiltinPrototypes.BLOCK_STATE,
				OperationFactory.create(Data::blockState)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_tool_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::tool)
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				MineBlockExperienceSource::parse
		);
	}

	private static Result<MineBlockExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData().andThen(rootElement ->
				LegacyCalculation.parse(rootElement, PROTOTYPE, context)
						.mapSuccess(MineBlockExperienceSource::new)
		);
	}

	public record Data(ServerPlayerEntity player, BlockState blockState, ItemStack tool) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}

	static {
		// Backwards compatibility.
		var legacy = new LegacyOperationRegistry<>(PROTOTYPE);
		legacy.registerBooleanFunction(
				"block",
				BlockStateCondition::parse,
				Data::blockState
		);
		legacy.registerBooleanFunction(
				"block_state",
				BlockStateCondition::parse,
				Data::blockState
		);
		legacy.registerBooleanFunction(
				"block_tag",
				LegacyBlockTagCondition::parse,
				Data::blockState
		);
		legacy.registerBooleanFunction(
				"tool",
				ItemStackCondition::parse,
				Data::tool
		);
		legacy.registerBooleanFunction(
				"tool_nbt",
				ItemStackCondition::parse,
				Data::tool
		);
		legacy.registerBooleanFunction(
				"tool_tag",
				LegacyItemTagCondition::parse,
				Data::tool
		);
		legacy.registerNumberFunction(
				"player_effect",
				effect -> (double) (effect.getAmplifier() + 1),
				EffectOperation::parse,
				Data::player
		);
		legacy.registerNumberFunction(
				"player_attribute",
				EntityAttributeInstance::getValue,
				AttributeOperation::parse,
				Data::player
		);

		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("player"),
				SkillsMod.createIdentifier("get_player")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("mined_block_state"),
				SkillsMod.createIdentifier("get_mined_block_state")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("tool_item_stack"),
				SkillsMod.createIdentifier("get_tool_item_stack")
		);
	}
}
