package net.puffish.skillsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.util.CommandUtils;

public class CategoryCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("category")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("lock")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.category())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var category = CategoryArgumentType.getCategory(context, "category");

											for (var player : players) {
												category.lock(player);
											}
											CommandUtils.sendSuccess(
													context,
													players,
													"category.lock",
													category.getId()
											);
											return players.size();
										})
								)
						)
				)
				.then(CommandManager.literal("unlock")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.category())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var category = CategoryArgumentType.getCategory(context, "category");

											for (var player : players) {
												category.unlock(player);
											}
											CommandUtils.sendSuccess(
													context,
													players,
													"category.unlock",
													category.getId()
											);
											return players.size();
										})
								)
						)
				)
				.then(CommandManager.literal("erase")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.category())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var category = CategoryArgumentType.getCategory(context, "category");

											for (var player : players) {
												category.erase(player);
											}
											CommandUtils.sendSuccess(
													context,
													players,
													"category.erase",
													category.getId()
											);
											return players.size();
										})
								)
						)
				)
				.then(CommandManager.literal("open")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.category())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var category = CategoryArgumentType.getCategory(context, "category");

											for (var player : players) {
												category.openScreen(player);
											}
											CommandUtils.sendSuccess(
													context,
													players,
													"category.open",
													category.getId()
											);
											return players.size();
										})
								)
						)
				);
	}
}
