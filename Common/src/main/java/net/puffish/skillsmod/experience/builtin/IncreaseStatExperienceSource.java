package net.puffish.skillsmod.experience.builtin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.experience.ExperienceSource;
import net.puffish.skillsmod.experience.calculation.CalculationManager;
import net.puffish.skillsmod.api.experience.calculation.condition.ConditionFactory;
import net.puffish.skillsmod.api.experience.calculation.condition.StatCondition;
import net.puffish.skillsmod.api.experience.calculation.parameter.EffectParameter;
import net.puffish.skillsmod.api.experience.calculation.parameter.ParameterFactory;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.Map;

public class IncreaseStatExperienceSource implements ExperienceSource {
	public static final Identifier ID = SkillsMod.createIdentifier("increase_stat");

	private static final Map<String, ConditionFactory<Context>> CONDITIONS = Map.ofEntries(
			Map.entry("stat", StatCondition.factory().map(p -> p.map(Context::stat)))
	);

	private static final Map<String, ParameterFactory<Context>> PARAMETERS = Map.ofEntries(
			Map.entry("player_effect", EffectParameter.factory().map(p -> p.map(Context::player))),
			Map.entry("amount", ParameterFactory.simple(ctx -> (double) ctx.amount()))
	);

	private final CalculationManager<Context> manager;

	private IncreaseStatExperienceSource(CalculationManager<Context> calculated) {
		this.manager = calculated;
	}

	public static void register() {
		SkillsAPI.registerExperienceSourceWithData(
				ID,
				(json, context) -> json.getAsObject().andThen(rootObjet -> IncreaseStatExperienceSource.create(rootObjet, context))
		);
	}

	private static Result<IncreaseStatExperienceSource, Failure> create(JsonObjectWrapper rootObject, ConfigContext context) {
		return CalculationManager.create(rootObject, CONDITIONS, PARAMETERS, context).mapSuccess(IncreaseStatExperienceSource::new);
	}

	private record Context(ServerPlayerEntity player, Stat<?> stat, int amount) { }

	public int getValue(ServerPlayerEntity player, Stat<?> stat, int amount) {
		return manager.getValue(new Context(player, stat, amount));
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
