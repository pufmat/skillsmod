package net.puffish.skillsmod.experience.source.builtin;

import net.minecraft.entity.LivingEntity;
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
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSource;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceDisposeContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.calculation.LegacyBuiltinPrototypes;
import net.puffish.skillsmod.calculation.LegacyCalculation;
import net.puffish.skillsmod.calculation.operation.LegacyOperationRegistry;
import net.puffish.skillsmod.calculation.operation.builtin.AttributeOperation;
import net.puffish.skillsmod.calculation.operation.builtin.DamageTypeCondition;
import net.puffish.skillsmod.calculation.operation.builtin.EffectOperation;
import net.puffish.skillsmod.calculation.operation.builtin.EntityTypeCondition;
import net.puffish.skillsmod.calculation.operation.builtin.ItemStackCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyDamageTypeTagCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyEntityTypeTagCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyItemTagCondition;
import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerChunk;
import net.puffish.skillsmod.experience.source.builtin.util.TamedActivity;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public record KillEntityExperienceSource(
		Calculation<Data> calculation,
		Optional<AntiFarmingPerChunk> antiFarmingPerChunk,
		TamedActivity tamedActivity
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("kill_entity");
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
				SkillsMod.createIdentifier("get_killed_living_entity"),
				BuiltinPrototypes.LIVING_ENTITY,
				OperationFactory.create(Data::entity)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_damage_source"),
				BuiltinPrototypes.DAMAGE_SOURCE,
				OperationFactory.create(Data::damageSource)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_dropped_experience"),
				BuiltinPrototypes.NUMBER,
				OperationFactory.create(Data::entityDroppedXp)
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				KillEntityExperienceSource::parse
		);
	}

	private static Result<KillEntityExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	private static Result<KillEntityExperienceSource, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optCalculation = LegacyCalculation.parse(rootObject, PROTOTYPE, context)
				.ifFailure(problems::add)
				.getSuccess();

		var antiFarmingPerChunk = rootObject.get("anti_farming_per_chunk")
				.orElse(LegacyUtils.wrapDeprecated(() -> rootObject.get("anti_farming"), 4, context))
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> AntiFarmingPerChunk.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
						.flatMap(Function.identity())
				);

		var tamed = rootObject.get("tamed")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> TamedActivity.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(TamedActivity.EXCLUDE);

		if (problems.isEmpty()) {
			return Result.success(new KillEntityExperienceSource(
					optCalculation.orElseThrow(),
					antiFarmingPerChunk,
					tamed
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public record Data(
			ServerPlayerEntity player,
			LivingEntity entity,
			ItemStack weapon,
			DamageSource damageSource,
			double entityDroppedXp
	) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}

	static {


		// Backwards compatibility.
		var legacy = new LegacyOperationRegistry<>(PROTOTYPE);
		legacy.registerBooleanFunction(
				"entity",
				EntityTypeCondition::parse,
				data -> data.entity().getType()
		);
		legacy.registerBooleanFunction(
				"entity_tag",
				LegacyEntityTypeTagCondition::parse,
				data -> data.entity().getType()
		);
		legacy.registerBooleanFunction(
				"weapon",
				ItemStackCondition::parse,
				Data::weapon
		);
		legacy.registerBooleanFunction(
				"weapon_nbt",
				ItemStackCondition::parse,
				Data::weapon
		);
		legacy.registerBooleanFunction(
				"weapon_tag",
				LegacyItemTagCondition::parse,
				Data::weapon
		);
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
				"entity_dropped_experience",
				Data::entityDroppedXp
		);
		legacy.registerNumberFunction(
				"entity_max_health",
				data -> (double) data.entity().getMaxHealth()
		);

		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("player"),
				SkillsMod.createIdentifier("get_player")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("weapon_item_stack"),
				SkillsMod.createIdentifier("get_weapon_item_stack")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("killed_living_entity"),
				SkillsMod.createIdentifier("get_killed_living_entity")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("damage_source"),
				SkillsMod.createIdentifier("get_damage_source")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("dropped_experience"),
				SkillsMod.createIdentifier("get_dropped_experience")
		);
	}
}
