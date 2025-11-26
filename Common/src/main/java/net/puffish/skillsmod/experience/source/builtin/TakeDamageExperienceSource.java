package net.puffish.skillsmod.experience.source.builtin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
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
import net.puffish.skillsmod.calculation.operation.builtin.DamageTypeCondition;
import net.puffish.skillsmod.calculation.operation.builtin.EffectOperation;
import net.puffish.skillsmod.calculation.operation.builtin.EntityTypeCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyDamageTypeTagCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyEntityTypeTagCondition;

import java.util.Optional;

public record TakeDamageExperienceSource(
		Calculation<Data> calculation
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("take_damage");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_weapon_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::weapon)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_damage_source"),
				BuiltinPrototypes.DAMAGE_SOURCE,
				OperationFactory.create(Data::damageSource)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_taken_damage"),
				BuiltinPrototypes.NUMBER,
				OperationFactory.create(data -> (double) data.damage())
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				TakeDamageExperienceSource::parse
		);
	}

	private static Result<TakeDamageExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData().andThen(rootElement ->
				LegacyCalculation.parse(rootElement, PROTOTYPE, context)
						.mapSuccess(TakeDamageExperienceSource::new)
		);
	}

	public record Data(
			ServerPlayerEntity player,
			ItemStack weapon,
			float damage,
			DamageSource damageSource
	) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}

	static {
		// Backwards compatibility.
		var legacy = new LegacyOperationRegistry<>(PROTOTYPE);
		legacy.registerBooleanFunction(
				"damage_type",
				DamageTypeCondition::parse,
				data -> data.damageSource().getType()
		);
		legacy.registerBooleanFunction(
				"damage_type_tag",
				LegacyDamageTypeTagCondition::parse,
				data -> data.damageSource().getType()
		);
		legacy.registerOptionalBooleanFunction(
				"attacker",
				EntityTypeCondition::parse,
				data -> Optional.ofNullable(data.damageSource().getAttacker()).map(Entity::getType)
		);
		legacy.registerOptionalBooleanFunction(
				"attacker_tag",
				LegacyEntityTypeTagCondition::parse,
				data -> Optional.ofNullable(data.damageSource().getAttacker()).map(Entity::getType)
		);
		legacy.registerOptionalBooleanFunction(
				"source",
				EntityTypeCondition::parse,
				data -> Optional.ofNullable(data.damageSource().getSource()).map(Entity::getType)
		);
		legacy.registerOptionalBooleanFunction(
				"source_tag",
				LegacyEntityTypeTagCondition::parse,
				data -> Optional.ofNullable(data.damageSource().getSource()).map(Entity::getType)
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
		legacy.registerNumberFunction(
				"damage",
				data -> (double) data.damage()
		);

		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("player"),
				SkillsMod.createIdentifier("get_player")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("damage_source"),
				SkillsMod.createIdentifier("get_damage_source")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("damage"),
				SkillsMod.createIdentifier("get_taken_damage")
		);
	}
}
