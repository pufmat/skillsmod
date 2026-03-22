package net.puffish.skillsmod.server.setup;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.puffish.skillsmod.SkillsMod;

public class SkillsGameRules {
	public static GameRule<Boolean> ANNOUNCE_NEW_POINTS = new GameRule<>(
			GameRuleCategory.CHAT,
			GameRuleType.BOOL,
			BoolArgumentType.bool(),
			GameRuleTypeVisitor::visitBoolean,
			Codec.BOOL,
			v -> v ? 1 : 0,
			true,
			FeatureFlagSet.of()
	);

	public static void register(ServerRegistrar registrar) {
		registrar.register(
				BuiltInRegistries.GAME_RULE,
				SkillsMod.createIdentifier("announce_new_points"),
				ANNOUNCE_NEW_POINTS
		);
	}
}
