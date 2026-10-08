package net.puffish.skillsmod.server.setup;

import net.minecraft.world.GameRules;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.mixin.BooleanRuleAccessor;

public record SkillsGameRules(
		GameRules.Key<GameRules.BooleanRule> announceNewPoints
) {
	public static SkillsGameRules register(ServerRegistrar registrar) {
		return new SkillsGameRules(
				registrar.registerGameRule(
						SkillsAPI.MOD_ID + ":" + "announceNewPoints",
						GameRules.Category.CHAT,
						BooleanRuleAccessor.puffish_skills$create(true)
				)
		);
	}
}
