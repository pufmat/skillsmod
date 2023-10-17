package net.puffish.skillsmod.experience.builtin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.mixin.LivingEntityInvoker;
import net.puffish.skillsmod.api.experience.ExperienceSource;
import net.puffish.skillsmod.api.experience.calculation.condition.DamageTypeCondition;
import net.puffish.skillsmod.api.experience.calculation.condition.DamageTypeTagCondition;
import net.puffish.skillsmod.experience.calculation.CalculationManager;
import net.puffish.skillsmod.api.experience.calculation.condition.ConditionFactory;
import net.puffish.skillsmod.api.experience.calculation.condition.EntityTypeCondition;
import net.puffish.skillsmod.api.experience.calculation.condition.EntityTypeTagCondition;
import net.puffish.skillsmod.api.experience.calculation.condition.ItemCondition;
import net.puffish.skillsmod.api.experience.calculation.condition.ItemNbtCondition;
import net.puffish.skillsmod.api.experience.calculation.condition.ItemTagCondition;
import net.puffish.skillsmod.api.experience.calculation.parameter.EffectParameter;
import net.puffish.skillsmod.api.experience.calculation.parameter.ParameterFactory;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class KillEntityExperienceSource implements ExperienceSource {
	public static final Identifier ID = SkillsMod.createIdentifier("kill_entity");

	private static final Map<String, ConditionFactory<Context>> CONDITIONS = Map.ofEntries(
			Map.entry("entity", EntityTypeCondition.factory().map(c -> c.map(Context::entityType))),
			Map.entry("entity_tag", EntityTypeTagCondition.factory().map(c -> c.map(Context::entityType))),
			Map.entry("weapon", ItemCondition.factory().map(c -> c.map(Context::weapon))),
			Map.entry("weapon_nbt", ItemNbtCondition.factory().map(c -> c.map(Context::weapon))),
			Map.entry("weapon_tag", ItemTagCondition.factory().map(c -> c.map(Context::weapon))),
			Map.entry("damage_type", DamageTypeCondition.factory().map(c -> c.map(Context::damageType))),
			Map.entry("damage_type_tag", DamageTypeTagCondition.factory().map(c -> c.map(Context::damageType)))
	);

	private static final Map<String, ParameterFactory<Context>> PARAMETERS = Map.ofEntries(
			Map.entry("player_effect", EffectParameter.factory().map(p -> p.map(Context::player))),
			Map.entry("entity_dropped_experience", ParameterFactory.simple(Context::entityDroppedXp)),
			Map.entry("entity_max_health", ParameterFactory.simple(Context::entityMaxHealth))
	);

	private final CalculationManager<Context> manager;
	private final Optional<AntiFarming> optAntiFarming;

	private KillEntityExperienceSource(CalculationManager<Context> calculated, Optional<AntiFarming> optAntiFarming) {
		this.manager = calculated;
		this.optAntiFarming = optAntiFarming;
	}

	public static void register() {
		SkillsAPI.registerExperienceSourceWithData(
				ID,
				(json, context) -> json.getAsObject().andThen(rootObject -> KillEntityExperienceSource.create(rootObject, context))
		);
	}

	private static Result<KillEntityExperienceSource, Failure> create(JsonObjectWrapper rootObject, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optCalculated = CalculationManager.create(rootObject, CONDITIONS, PARAMETERS, context)
				.ifFailure(failures::add)
				.getSuccess();

		var optAntiFarming = rootObject.get("anti_farming")
				.getSuccess()
				.flatMap(element -> AntiFarming.parse(element)
						.ifFailure(failures::add)
						.getSuccess()
						.flatMap(Function.identity())
				);

		if (failures.isEmpty()) {
			return Result.success(new KillEntityExperienceSource(
					optCalculated.orElseThrow(),
					optAntiFarming
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public record AntiFarming(int limitPerChunk, int resetAfterSeconds) {
		public static Result<Optional<AntiFarming>, Failure> parse(JsonElementWrapper rootElement) {
			return rootElement.getAsObject()
					.andThen(AntiFarming::parse);
		}

		public static Result<Optional<AntiFarming>, Failure> parse(JsonObjectWrapper rootObject) {
			var failures = new ArrayList<Failure>();

			// Deprecated
			var enabled = rootObject.getBoolean("enabled")
					.getSuccess()
					.orElse(true);

			var optLimitPerChunk = rootObject.getInt("limit_per_chunk")
					.ifFailure(failures::add)
					.getSuccess();

			var optResetAfterSeconds = rootObject.getInt("reset_after_seconds")
					.ifFailure(failures::add)
					.getSuccess();

			if (failures.isEmpty()) {
				if (enabled) {
					return Result.success(Optional.of(new AntiFarming(
							optLimitPerChunk.orElseThrow(),
							optResetAfterSeconds.orElseThrow()
					)));
				} else {
					return Result.success(Optional.empty());
				}
			} else {
				return Result.failure(ManyFailures.ofList(failures));
			}
		}
	}

	private record Context(ServerPlayerEntity player, LivingEntity entity, ItemStack weapon, DamageSource damageSource) {
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

		String damageType() {
			return damageSource.getName();
		}
	}

	public int getValue(ServerPlayerEntity player, LivingEntity entity, ItemStack weapon, DamageSource damageSource) {
		return manager.getValue(new Context(player, entity, weapon, damageSource));
	}

	public Optional<AntiFarming> getAntiFarming() {
		return optAntiFarming;
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
