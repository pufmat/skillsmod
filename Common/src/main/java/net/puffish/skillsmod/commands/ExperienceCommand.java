package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.utils.CommandUtils;

public class ExperienceCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("experience")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("add")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", IdentifierArgumentType.identifier())
										.then(CommandManager.argument("amount", IntegerArgumentType.integer())
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var categoryId = IdentifierArgumentType.getIdentifier(context, "category");
													var amount = IntegerArgumentType.getInteger(context, "amount");

													var category = CommandUtils.getCategory(categoryId);

													for (var player : players) {
														category.addExperience(player, amount);
													}
													return CommandUtils.sendSuccess(
															context,
															players,
															"experience.add",
															amount,
															categoryId
													);
												})
										)
								)
						)
				)
				.then(CommandManager.literal("set")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", IdentifierArgumentType.identifier())
										.then(CommandManager.argument("amount", IntegerArgumentType.integer())
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var categoryId = IdentifierArgumentType.getIdentifier(context, "category");
													var amount = IntegerArgumentType.getInteger(context, "amount");

													var category = CommandUtils.getCategory(categoryId);

													for (var player : players) {
														category.setExperience(player, amount);
													}
													return CommandUtils.sendSuccess(
															context,
															players,
															"experience.set",
															amount,
															categoryId
													);
												})
										)
								)
						)
				);
	}
}
