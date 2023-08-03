package net.puffish.skillsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.utils.CommandUtils;

public class CategoryCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("category")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("lock")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", IdentifierArgumentType.identifier())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var categoryId = IdentifierArgumentType.getIdentifier(context, "category");

											var category = CommandUtils.getCategory(categoryId);

											for (var player : players) {
												category.lock(player);
											}
											return CommandUtils.sendSuccess(
													context,
													players,
													"category.lock",
													categoryId
											);
										})
								)
						)
				)
				.then(CommandManager.literal("unlock")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", IdentifierArgumentType.identifier())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var categoryId = IdentifierArgumentType.getIdentifier(context, "category");

											var category = CommandUtils.getCategory(categoryId);

											for (var player : players) {
												category.unlock(player);
											}
											return CommandUtils.sendSuccess(
													context,
													players,
													"category.unlock",
													categoryId
											);
										})
								)
						)
				)
				.then(CommandManager.literal("erase")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", IdentifierArgumentType.identifier())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var categoryId = IdentifierArgumentType.getIdentifier(context, "category");

											var category = CommandUtils.getCategory(categoryId);

											for (var player : players) {
												category.erase(player);
											}
											return CommandUtils.sendSuccess(
													context,
													players,
													"category.erase",
													categoryId
											);
										})
								)
						)
				);
	}
}
