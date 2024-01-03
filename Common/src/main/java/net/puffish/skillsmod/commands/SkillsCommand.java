package net.puffish.skillsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.commands.arguments.SkillArgumentType;
import net.puffish.skillsmod.utils.CommandUtils;

public class SkillsCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("skills")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("unlock")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.category())
										.then(CommandManager.argument("skill", SkillArgumentType.skillFromCategory("category"))
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var category = CategoryArgumentType.getCategory(context, "category");
													var skill = SkillArgumentType.getSkillFromCategory(context, "skill", category);

													for (var player : players) {
														skill.unlock(player);
													}
													return CommandUtils.sendSuccess(
															context,
															players,
															"skills.unlock",
															category.getId(),
															skill.getId()
													);
												})
										)
								)
						)
				)
				.then(CommandManager.literal("lock")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.category())
										.then(CommandManager.argument("skill", SkillArgumentType.skillFromCategory("category"))
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var category = CategoryArgumentType.getCategory(context, "category");
													var skill = SkillArgumentType.getSkillFromCategory(context, "skill", category);

													for (var player : players) {
														skill.lock(player);
													}
													return CommandUtils.sendSuccess(
															context,
															players,
															"skills.lock",
															category.getId(),
															skill.getId()
													);
												})
										)
								)
						)
				)
				.then(CommandManager.literal("reset")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.category())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var category = CategoryArgumentType.getCategory(context, "category");

											for (var player : players) {
												category.resetSkills(player);
											}
											return CommandUtils.sendSuccess(
													context,
													players,
													"skills.reset",
													category.getId()
											);
										})
								)
						)
				);
	}
}
