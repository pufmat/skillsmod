package net.puffish.skillsmod.experience.source.builtin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
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
import net.puffish.skillsmod.calculation.operation.builtin.EffectOperation;
import net.puffish.skillsmod.calculation.operation.builtin.ItemStackCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyItemTagCondition;

public record EatFoodExperienceSource(
		Calculation<Data> calculation
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("eat_food");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_eaten_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::itemStack)
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				EatFoodExperienceSource::parse
		);
	}

	private static Result<EatFoodExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData().andThen(rootElement ->
				LegacyCalculation.parse(rootElement, PROTOTYPE, context)
						.mapSuccess(EatFoodExperienceSource::new)
		);
	}

	public record Data(ServerPlayer player, ItemStack itemStack) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}

	static {


		// Backwards compatibility.
		var legacy = new LegacyOperationRegistry<>(PROTOTYPE);
		legacy.registerBooleanFunction(
				"item",
				ItemStackCondition::parse,
				Data::itemStack
		);
		legacy.registerBooleanFunction(
				"item_nbt",
				ItemStackCondition::parse,
				Data::itemStack
		);
		legacy.registerBooleanFunction(
				"item_tag",
				LegacyItemTagCondition::parse,
				Data::itemStack
		);
		legacy.registerNumberFunction(
				"player_effect",
				effect -> (double) (effect.getAmplifier() + 1),
				EffectOperation::parse,
				Data::player
		);
		legacy.registerNumberFunction(
				"player_attribute",
				AttributeInstance::getValue,
				AttributeOperation::parse,
				Data::player
		);
		legacy.registerNumberFunction(
				"food_hunger",
				data -> {
					var fc = data.itemStack().getComponents().get(DataComponents.FOOD);
					return fc == null ? 0.0 : fc.nutrition();
				}
		);
		legacy.registerNumberFunction(
				"food_saturation",
				data -> {
					var fc = data.itemStack().get(DataComponents.FOOD);
					return fc == null ? 0.0 : fc.saturation();
				}
		);

		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("player"),
				SkillsMod.createIdentifier("get_player")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("eaten_item_stack"),
				SkillsMod.createIdentifier("get_eaten_item_stack")
		);
	}
}
