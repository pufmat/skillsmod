package net.puffish.skillsmod.server.setup;

import net.minecraft.world.GameRules;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.mixin.BooleanRuleInvoker;

public class SkillsGameRules {
	public static final GameRules.Key<GameRules.BooleanRule> ANNOUNCE_NEW_POINTS = create(
			"announceNewPoints",
			GameRules.Category.CHAT
	);

	private static <T extends GameRules.Rule<T>> GameRules.Key<T> create(String name, GameRules.Category category) {
		return new GameRules.Key<>(SkillsAPI.MOD_ID + ":" + name, category);
	}

	public static void register(ServerRegistrar registrar) {
		registrar.registerGameRule(
				ANNOUNCE_NEW_POINTS,
				BooleanRuleInvoker.invokeCreate(true)
		);
	}
}
