package net.puffish.skillsmod.utils;

import com.google.common.collect.ObjectArrays;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.SkillsMod;

import java.util.Collection;

public class CommandUtils {

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
