package net.puffish.skillsmod.experience.source.builtin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.api.calculation.Variables;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.experience.source.ExperienceSource;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceDisposeContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerChunk;
import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerEntity;
import net.puffish.skillsmod.experience.source.builtin.util.TamedActivity;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record DealDamageExperienceSource(
		Calculation<Data> calculation,
		Optional<AntiFarmingPerEntity> antiFarmingPerEntity,
		Optional<AntiFarmingPerChunk> antiFarmingPerChunk,
		TamedActivity tamedActivity
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("deal_damage");
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
				SkillsMod.createIdentifier("get_damaged_living_entity"),
				BuiltinPrototypes.LIVING_ENTITY,
				OperationFactory.create(Data::entity)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_damage_source"),
				BuiltinPrototypes.DAMAGE_SOURCE,
				OperationFactory.create(Data::damageSource)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_dealt_damage"),
				BuiltinPrototypes.NUMBER,
				OperationFactory.create(data -> (double) data.damage())
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				DealDamageExperienceSource::parse
		);
	}

	private static Result<DealDamageExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	private static Result<DealDamageExperienceSource, Problem> parse(JsonObject rootObject, ExperienceSourceConfigContext context) {
		var problems = new ArrayList<Problem>();

		var variables = rootObject.get("variables")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(variablesElement -> Variables.parse(variablesElement, PROTOTYPE, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(() -> Variables.create(Map.of()));

		var optCalculation = rootObject.get("experience")
				.andThen(experienceElement -> Calculation.parse(
						experienceElement,
						variables,
						context
				))
				.ifFailure(problems::add)
				.getSuccess();

		var antiFarmingPerEntity = rootObject.get("anti_farming_per_entity")
				.orElse(LegacyUtils.wrapDeprecated(() -> rootObject.get("anti_farming"), 4, context))
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> AntiFarmingPerEntity.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
				);

		var antiFarmingPerChunk = rootObject.get("anti_farming_per_chunk")
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
			return Result.success(new DealDamageExperienceSource(
					optCalculation.orElseThrow(),
					antiFarmingPerEntity,
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
			float damage,
			DamageSource damageSource
	) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}
}
