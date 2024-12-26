package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.util.CommandUtils;

public class PointsCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("points")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("add")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.category())
										.then(CommandManager.argument("count", IntegerArgumentType.integer())
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var category = CategoryArgumentType.getCategory(context, "category");
													var count = IntegerArgumentType.getInteger(context, "count");

													for (var player : players) {
														category.addExtraPoints(player, count);
													}
													CommandUtils.sendSuccess(
															context,
															players,
															"points.add",
															count,
															category.getId()
													);
													return players.size();
												})
										)
								)
						)
				)
				.then(CommandManager.literal("set")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.category())
										.then(CommandManager.argument("count", IntegerArgumentType.integer())
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var category = CategoryArgumentType.getCategory(context, "category");
													var count = IntegerArgumentType.getInteger(context, "count");

													for (var player : players) {
														category.setExtraPoints(player, count);
													}
													CommandUtils.sendSuccess(
															context,
															players,
															"points.set",
															count,
															category.getId()
													);
													return players.size();
												})
										)
								)
						)
				)
				.then(CommandManager.literal("get")
						.then(CommandManager.argument("player", EntityArgumentType.player())
								.then(CommandManager.argument("category", CategoryArgumentType.category())
										.executes(context -> {
											var player = EntityArgumentType.getPlayer(context, "player");
											var category = CategoryArgumentType.getCategory(context, "category");

											var count = category.getExtraPoints(player);
											CommandUtils.sendSuccess(
													context,
													player,
													"points.get",
													count,
													category.getId()
											);
											return count;
										})
								)
						)
				);
	}
}
