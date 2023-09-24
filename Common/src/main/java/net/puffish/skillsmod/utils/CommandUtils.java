package net.puffish.skillsmod.utils;

import com.google.common.collect.ObjectArrays;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;

import java.util.Collection;

public class CommandUtils {
	public static Category getCategory(Identifier categoryId) throws CommandSyntaxException {
		return SkillsAPI.getCategory(categoryId).orElseThrow(() -> new SimpleCommandExceptionType(
				SkillsMod.createTranslatable("command", "no_such_category", categoryId)
		).create());
	}

	public static Skill getSkill(Category category, String skillId) throws CommandSyntaxException {
		return category.getSkill(skillId).orElseThrow(() -> new SimpleCommandExceptionType(
				SkillsMod.createTranslatable("command", "no_such_skill", skillId)
		).create());
	}

	public static int sendSuccess(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> players, String command, Object... args) {
		if (players.size() == 1) {
			context.getSource().sendFeedback(() -> SkillsMod.createTranslatable(
					"command", command + ".success.single", ObjectArrays.concat(args, players.iterator().next().getDisplayName())
			), true);
		} else {
			context.getSource().sendFeedback(() -> SkillsMod.createTranslatable(
					"command", command + ".success.multiple", ObjectArrays.concat(args, players.size())
			), true);
		}
		return players.size();
	}
}
