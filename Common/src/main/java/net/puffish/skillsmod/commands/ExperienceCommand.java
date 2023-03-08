package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.SkillsAPI;

public class ExperienceCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("experience")
				.then(CommandManager.literal("add")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", StringArgumentType.string())
										.then(CommandManager.argument("amount", IntegerArgumentType.integer())
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var categoryId = StringArgumentType.getString(context, "category");
													var amount = IntegerArgumentType.getInteger(context, "amount");
													for (var player : players) {
														SkillsAPI.addExperience(player, categoryId, amount);
													}
													return players.size();
												})
										)
								)
						)
				)
				.then(CommandManager.literal("set")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", StringArgumentType.string())
										.then(CommandManager.argument("amount", IntegerArgumentType.integer())
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var categoryId = StringArgumentType.getString(context, "category");
													var amount = IntegerArgumentType.getInteger(context, "amount");
													for (var player : players) {
														SkillsAPI.setExperience(player, categoryId, amount);
													}
													return players.size();
												})
										)
								)
						)
				);
	}
}
