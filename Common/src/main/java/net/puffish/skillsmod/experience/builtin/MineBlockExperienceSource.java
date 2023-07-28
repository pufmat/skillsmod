package net.puffish.skillsmod.experience.builtin;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.experience.ExperienceSource;
import net.puffish.skillsmod.experience.calculation.CalculationManager;
import net.puffish.skillsmod.experience.calculation.condition.BlockCondition;
import net.puffish.skillsmod.experience.calculation.condition.BlockStateCondition;
import net.puffish.skillsmod.experience.calculation.condition.BlockTagCondition;
import net.puffish.skillsmod.experience.calculation.condition.ConditionFactory;
import net.puffish.skillsmod.experience.calculation.condition.ItemCondition;
import net.puffish.skillsmod.experience.calculation.condition.ItemNbtCondition;
import net.puffish.skillsmod.experience.calculation.condition.ItemTagCondition;
import net.puffish.skillsmod.experience.calculation.parameter.EffectParameter;
import net.puffish.skillsmod.experience.calculation.parameter.ParameterFactory;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;

import java.util.Map;

public class MineBlockExperienceSource implements ExperienceSource {
	public static final Identifier ID = SkillsMod.createIdentifier("mine_block");

	private static final Map<String, ConditionFactory<Context>> CONDITIONS = Map.ofEntries(
			Map.entry("block", BlockCondition.factory().map(c -> c.map(Context::blockState))),
			Map.entry("block_state", BlockStateCondition.factory().map(c -> c.map(Context::blockState))),
			Map.entry("block_tag", BlockTagCondition.factory().map(c -> c.map(Context::blockState))),
			Map.entry("tool", ItemCondition.factory().map(c -> c.map(Context::tool))),
			Map.entry("tool_nbt", ItemNbtCondition.factory().map(c -> c.map(Context::tool))),
			Map.entry("tool_tag", ItemTagCondition.factory().map(c -> c.map(Context::tool)))
	);

	private static final Map<String, ParameterFactory<Context>> PARAMETERS = Map.ofEntries(
			Map.entry("player_effect", EffectParameter.factory().map(p -> p.map(Context::player)))
	);

	private final CalculationManager<Context> manager;

	private MineBlockExperienceSource(CalculationManager<Context> calculated) {
		this.manager = calculated;
	}

	public static void register() {
		SkillsAPI.registerExperienceSourceWithData(
				ID,
				(json, context) -> json.getAsObject().andThen(rootObject -> MineBlockExperienceSource.create(rootObject, context))
		);
	}

	private static Result<MineBlockExperienceSource, Failure> create(JsonObjectWrapper rootObject, ConfigContext context) {
		return CalculationManager.create(rootObject, CONDITIONS, PARAMETERS, context).mapSuccess(MineBlockExperienceSource::new);
	}

	private record Context(ServerPlayerEntity player, BlockState blockState, ItemStack tool) {

	}

	public int getValue(ServerPlayerEntity player, BlockState blockState, ItemStack tool) {
		return manager.getValue(new Context(player, blockState, tool));
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
