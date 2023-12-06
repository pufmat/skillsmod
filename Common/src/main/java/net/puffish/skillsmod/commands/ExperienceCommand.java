package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.utils.CommandUtils;

public class ExperienceCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("experience")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("add")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.categoryOnlyWithExperience())
										.then(CommandManager.argument("amount", IntegerArgumentType.integer())
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var category = CategoryArgumentType.getCategory(context, "category");
													var amount = IntegerArgumentType.getInteger(context, "amount");

													var experience = category.getExperience().orElseThrow();

													for (var player : players) {
														experience.addTotal(player, amount);
													}
													return CommandUtils.sendSuccess(
															context,
															players,
															"experience.add",
															amount,
															category.getId()
													);
												})
										)
								)
						)
				)
				.then(CommandManager.literal("set")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.categoryOnlyWithExperience())
										.then(CommandManager.argument("amount", IntegerArgumentType.integer())
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var category = CategoryArgumentType.getCategory(context, "category");
													var amount = IntegerArgumentType.getInteger(context, "amount");

													var experience = category.getExperience().orElseThrow();

													for (var player : players) {
														experience.setTotal(player, amount);
													}
													return CommandUtils.sendSuccess(
															context,
															players,
															"experience.set",
															amount,
															category.getId()
													);
												})
										)
								)
						)
				);
	}
}
