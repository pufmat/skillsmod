package net.puffish.skillsmod.server;

import net.minecraft.world.GameRules;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.mixin.BooleanRuleInvoker;
import net.puffish.skillsmod.server.setup.ServerGameRules;

public class SkillsGameRules {
	public static GameRules.Key<GameRules.BooleanRule> ANNOUNCE_NEW_POINTS;

	public static void register(ServerGameRules gameRules) {
		ANNOUNCE_NEW_POINTS = gameRules.registerGameRule(SkillsAPI.MOD_ID, "announceNewPoints", GameRules.Category.CHAT, BooleanRuleInvoker.invokeCreate(true));
	}
}
