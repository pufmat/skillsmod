package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.SkillsAPI;

public class CategoryCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("category")
				.then(CommandManager.literal("lock")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", StringArgumentType.string())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var categoryId = StringArgumentType.getString(context, "category");
											for (var player : players) {
												SkillsAPI.lockCategory(player, categoryId);
											}
											return players.size();
										})
								)
						)
				)
				.then(CommandManager.literal("unlock")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", StringArgumentType.string())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var categoryId = StringArgumentType.getString(context, "category");
											for (var player : players) {
												SkillsAPI.unlockCategory(player, categoryId);
											}
											return players.size();
										})
								)
						)
				)
				.then(CommandManager.literal("erase")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", StringArgumentType.string())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var categoryId = StringArgumentType.getString(context, "category");
											for (var player : players) {
												SkillsAPI.eraseCategory(player, categoryId);
											}
											return players.size();
										})
								)
						)
				);
	}
}
