package net.puffish.skillsmod.experience.builtin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.experience.ExperienceSource;
import net.puffish.skillsmod.experience.calculation.CalculationManager;
import net.puffish.skillsmod.api.experience.calculation.condition.ConditionFactory;
import net.puffish.skillsmod.api.experience.calculation.condition.ItemCondition;
import net.puffish.skillsmod.api.experience.calculation.condition.ItemNbtCondition;
import net.puffish.skillsmod.api.experience.calculation.condition.ItemTagCondition;
import net.puffish.skillsmod.api.experience.calculation.parameter.EffectParameter;
import net.puffish.skillsmod.api.experience.calculation.parameter.ParameterFactory;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.Map;

public class CraftItemExperienceSource implements ExperienceSource {

	public static final Identifier ID = SkillsMod.createIdentifier("craft_item");

	private static final Map<String, ConditionFactory<Context>> CONDITIONS = Map.ofEntries(
			Map.entry("item", ItemCondition.factory().map(c -> c.map(Context::item))),
			Map.entry("item_nbt", ItemNbtCondition.factory().map(c -> c.map(Context::item))),
			Map.entry("item_tag", ItemTagCondition.factory().map(c -> c.map(Context::item)))
	);

	private static final Map<String, ParameterFactory<Context>> PARAMETERS = Map.ofEntries(
			Map.entry("player_effect", EffectParameter.factory().map(p -> p.map(Context::player)))
	);

	private final CalculationManager<Context> manager;

	private CraftItemExperienceSource(CalculationManager<Context> calculated) {
		this.manager = calculated;
	}

	public static void register() {
		SkillsAPI.registerExperienceSourceWithData(
				ID,
				(json, context) -> json.getAsObject().andThen(rootObject -> CraftItemExperienceSource.create(rootObject, context))
		);
	}

	private static Result<CraftItemExperienceSource, Failure> create(JsonObjectWrapper rootObject, ConfigContext context) {
		return CalculationManager.create(rootObject, CONDITIONS, PARAMETERS, context).mapSuccess(CraftItemExperienceSource::new);
	}

	private record Context(ServerPlayerEntity player, ItemStack item) {

	}

	public int getValue(ServerPlayerEntity player, ItemStack itemStack) {
		return manager.getValue(new Context(player, itemStack));
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
