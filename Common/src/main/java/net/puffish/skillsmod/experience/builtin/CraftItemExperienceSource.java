package net.puffish.skillsmod.experience.builtin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.experience.ExperienceSource;
import net.puffish.skillsmod.experience.calculation.CalculationManager;
import net.puffish.skillsmod.experience.calculation.condition.ConditionFactory;
import net.puffish.skillsmod.experience.calculation.condition.ItemCondition;
import net.puffish.skillsmod.experience.calculation.condition.ItemTagCondition;
import net.puffish.skillsmod.experience.calculation.parameter.EffectParameter;
import net.puffish.skillsmod.experience.calculation.parameter.ParameterFactory;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;

import java.util.Map;

public class CraftItemExperienceSource implements ExperienceSource {

	public static final Identifier ID = SkillsMod.createIdentifier("craft_item");

	private static final Map<String, ConditionFactory<Context>> CONDITIONS = Map.ofEntries(
			Map.entry("item", ConditionFactory.map(ItemCondition::parse, Context::item)),
			Map.entry("item_tag", ConditionFactory.map(ItemTagCondition::parse, Context::item))
	);

	private static final Map<String, ParameterFactory<Context>> PARAMETERS = Map.ofEntries(
			Map.entry("player_effect", ParameterFactory.map(EffectParameter::parse, Context::player))
	);

	private final CalculationManager<Context> manager;

	private CraftItemExperienceSource(CalculationManager<Context> calculated) {
		this.manager = calculated;
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				maybeDataElement -> maybeDataElement
						.andThen(JsonElementWrapper::getAsObject)
						.andThen(CraftItemExperienceSource::create)
		);
	}
	private static Result<CraftItemExperienceSource, Error> create(JsonObjectWrapper rootObject) {
		return CalculationManager.create(rootObject, CONDITIONS, PARAMETERS).mapSuccess(CraftItemExperienceSource::new);
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
