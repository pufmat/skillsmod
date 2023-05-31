package net.puffish.skillsmod.experience.builtin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.mixin.LivingEntityInvoker;
import net.puffish.skillsmod.experience.ExperienceSource;
import net.puffish.skillsmod.experience.calculation.CalculationManager;
import net.puffish.skillsmod.experience.calculation.condition.ConditionFactory;
import net.puffish.skillsmod.experience.calculation.condition.EntityCondition;
import net.puffish.skillsmod.experience.calculation.condition.EntityTagCondition;
import net.puffish.skillsmod.experience.calculation.condition.ItemCondition;
import net.puffish.skillsmod.experience.calculation.condition.ItemNbtCondition;
import net.puffish.skillsmod.experience.calculation.condition.ItemTagCondition;
import net.puffish.skillsmod.experience.calculation.parameter.EffectParameter;
import net.puffish.skillsmod.experience.calculation.parameter.ParameterFactory;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;
import java.util.Map;

public class KillEntityExperienceSource implements ExperienceSource {
	public static final Identifier ID = SkillsMod.createIdentifier("kill_entity");

	private static final Map<String, ConditionFactory<Context>> CONDITIONS = Map.ofEntries(
			Map.entry("entity", ConditionFactory.map(EntityCondition::parse, Context::entityType)),
			Map.entry("entity_tag", ConditionFactory.map(EntityTagCondition::parse, Context::entityType)),
			Map.entry("weapon", ConditionFactory.map(ItemCondition::parse, Context::weapon)),
			Map.entry("weapon_nbt", ConditionFactory.map(ItemNbtCondition::parse, Context::weapon)),
			Map.entry("weapon_tag", ConditionFactory.map(ItemTagCondition::parse, Context::weapon))
	);

	private static final Map<String, ParameterFactory<Context>> PARAMETERS = Map.ofEntries(
			Map.entry("player_effect", ParameterFactory.map(EffectParameter::parse, Context::player)),
			Map.entry("entity_dropped_experience", ParameterFactory.simple(Context::entityDroppedXp)),
			Map.entry("entity_max_health", ParameterFactory.simple(Context::entityMaxHealth))
	);

	private final CalculationManager<Context> manager;
	private final AntiFarming antiFarming;

	private KillEntityExperienceSource(CalculationManager<Context> calculated, AntiFarming antiFarming) {
		this.manager = calculated;
		this.antiFarming = antiFarming;
	}

	public static void register() {
		SkillsAPI.registerExperienceSourceWithData(
				ID,
				json -> json.getAsObject().andThen(KillEntityExperienceSource::create)
		);
	}

	private static Result<KillEntityExperienceSource, Error> create(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optCalculated = CalculationManager.create(rootObject, CONDITIONS, PARAMETERS)
				.ifFailure(errors::add)
				.getSuccess();

		var optAntiFarming = rootObject.get("anti_farming")
				.andThen(AntiFarming::parse)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new KillEntityExperienceSource(
					optCalculated.orElseThrow(),
					optAntiFarming.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	public record AntiFarming(boolean enabled, int limitPerChunk, int resetAfterSeconds) {
		public static Result<AntiFarming, Error> parse(JsonElementWrapper rootElement) {
			return rootElement.getAsObject()
					.andThen(AntiFarming::parse);
		}

		public static Result<AntiFarming, Error> parse(JsonObjectWrapper rootObject) {
			var errors = new ArrayList<Error>();

			var enabled = rootObject.getBoolean("enabled")
					.ifFailure(errors::add)
					.getSuccess();

			var limitPerChunk = rootObject.getInt("limit_per_chunk")
					.ifFailure(errors::add)
					.getSuccess();

			var resetAfterSeconds = rootObject.getInt("reset_after_seconds")
					.ifFailure(errors::add)
					.getSuccess();

			if (errors.isEmpty()) {
				return Result.success(new AntiFarming(
						enabled.orElseThrow(),
						limitPerChunk.orElseThrow(),
						resetAfterSeconds.orElseThrow()
				));
			} else {
				return Result.failure(ManyErrors.ofList(errors));
			}
		}
	}

	private record Context(ServerPlayerEntity player, LivingEntity entity, ItemStack weapon) {
		public double entityDroppedXp() {
			var entityAccess = (LivingEntityInvoker) entity;
			return entityAccess.invokeShouldDropXp() ? entityAccess.invokeGetXpToDrop(player) : 0.0;
		}
		public double entityMaxHealth() {
			return entity.getMaxHealth();
		}
		public EntityType<?> entityType() {
			return entity.getType();
		}
	}

	public int getValue(ServerPlayerEntity player, LivingEntity entity, ItemStack weapon) {
		return manager.getValue(new Context(player, entity, weapon));
	}

	public AntiFarming getAntiFarming() {
		return antiFarming;
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
