package net.puffish.skillsmod.server.setup;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import net.minecraft.world.rule.GameRuleType;
import net.minecraft.world.rule.GameRuleVisitor;
import net.puffish.skillsmod.SkillsMod;

public class SkillsGameRules {
	public static GameRule<Boolean> ANNOUNCE_NEW_POINTS = new GameRule<>(
			GameRuleCategory.CHAT,
			GameRuleType.BOOL,
			BoolArgumentType.bool(),
			GameRuleVisitor::visitBoolean,
			Codec.BOOL,
			v -> v ? 1 : 0,
			true,
			FeatureSet.empty()
	);

	public static void register(ServerRegistrar registrar) {
		registrar.register(
				Registries.GAME_RULE,
				SkillsMod.createIdentifier("announce_new_points"),
				ANNOUNCE_NEW_POINTS
		);
	}
}
