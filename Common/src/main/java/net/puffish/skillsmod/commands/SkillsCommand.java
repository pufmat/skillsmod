package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.SkillsAPI;

public class SkillsCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("skills")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("unlock")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", StringArgumentType.string())
										.then(CommandManager.argument("skill", StringArgumentType.string())
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var categoryId = StringArgumentType.getString(context, "category");
													var skillId = StringArgumentType.getString(context, "skill");
													for (var player : players) {
														SkillsAPI.unlockSkill(player, categoryId, skillId);
													}
													return players.size();
												})
										)
								)
						)
				)
				.then(CommandManager.literal("reset")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", StringArgumentType.string())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var categoryId = StringArgumentType.getString(context, "category");
											for (var player : players) {
												SkillsAPI.resetSkills(player, categoryId);
											}
											return players.size();
										})
								)
						)
				);
	}
}
