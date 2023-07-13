package net.puffish.skillsmod.experience.builtin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.experience.ExperienceSource;
import net.puffish.skillsmod.experience.calculation.CalculationManager;
import net.puffish.skillsmod.experience.calculation.condition.ConditionFactory;
import net.puffish.skillsmod.experience.calculation.condition.DamageTypeCondition;
import net.puffish.skillsmod.experience.calculation.condition.EntityTypeCondition;
import net.puffish.skillsmod.experience.calculation.condition.EntityTypeTagCondition;
import net.puffish.skillsmod.experience.calculation.parameter.EffectParameter;
import net.puffish.skillsmod.experience.calculation.parameter.ParameterFactory;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

import java.util.Map;
import java.util.Optional;

public class TakeDamageExperienceSource implements ExperienceSource {
	public static final Identifier ID = SkillsMod.createIdentifier("take_damage");

	private static final Map<String, ConditionFactory<Context>> CONDITIONS = Map.ofEntries(
			Map.entry("damage_type", DamageTypeCondition.factory().map(c -> c.map(Context::damageType))),
			Map.entry("attacker", EntityTypeCondition.factory().map(c -> ctx -> ctx.attacker().map(c::test).orElse(false))),
			Map.entry("attacker_tag", EntityTypeTagCondition.factory().map(c -> ctx -> ctx.attacker().map(c::test).orElse(false))),
			Map.entry("source", EntityTypeCondition.factory().map(c -> ctx -> ctx.source().map(c::test).orElse(false))),
			Map.entry("source_tag", EntityTypeTagCondition.factory().map(c -> ctx -> ctx.source().map(c::test).orElse(false)))
	);

	private static final Map<String, ParameterFactory<Context>> PARAMETERS = Map.ofEntries(
			Map.entry("player_effect", EffectParameter.factory().map(p -> p.map(Context::player))),
			Map.entry("damage", ParameterFactory.simple(ctx -> (double) ctx.damage()))
	);

	private final CalculationManager<Context> manager;

	private TakeDamageExperienceSource(CalculationManager<Context> calculated) {
		this.manager = calculated;
	}

	public static void register() {
		SkillsAPI.registerExperienceSourceWithData(
				ID,
				(json, context) -> json.getAsObject().andThen(rootObjet -> TakeDamageExperienceSource.create(rootObjet, context))
		);
	}

	private static Result<TakeDamageExperienceSource, Error> create(JsonObjectWrapper rootObject, ConfigContext context) {
		return CalculationManager.create(rootObject, CONDITIONS, PARAMETERS, context).mapSuccess(TakeDamageExperienceSource::new);
	}

	private record Context(ServerPlayerEntity player, float damage, DamageSource damageSource) {
		String damageType() {
			return damageSource.getName();
		}
		Optional<EntityType<?>> source() {
			return Optional.ofNullable(damageSource.getSource()).map(Entity::getType);
		}
		Optional<EntityType<?>> attacker() {
			return Optional.ofNullable(damageSource.getAttacker()).map(Entity::getType);
		}
	}

	public int getValue(ServerPlayerEntity player, float damage, DamageSource damageSource) {
		return manager.getValue(new Context(player, damage, damageSource));
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
