package net.puffish.skillsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.commands.arguments.SkillArgumentType;
import net.puffish.skillsmod.util.CommandUtils;

public class SkillsCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> create() {
		return Commands.literal("skills")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("unlock")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.category())
										.then(Commands.argument("skill", SkillArgumentType.skillFromCategory("category"))
												.executes(SkillsCommand::unlock)
										)
								)
						)
				)
				.then(Commands.literal("lock")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.category())
										.then(Commands.argument("skill", SkillArgumentType.skillFromCategory("category"))
												.executes(SkillsCommand::lock)
										)
								)
						)
				)
				.then(Commands.literal("reset")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.category())
										.executes(SkillsCommand::reset)
								)
						)
				);
	}

	private static int unlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");
		var skill = SkillArgumentType.getSkillFromCategory(context, "skill", category);

		for (var player : players) {
			skill.unlock(player);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"skills.unlock",
				category.getId(),
				skill.getId()
		);
		return players.size();
	}

	private static int lock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");
		var skill = SkillArgumentType.getSkillFromCategory(context, "skill", category);

		for (var player : players) {
			skill.lock(player);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"skills.lock",
				category.getId(),
				skill.getId()
		);
		return players.size();
	}

	private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");

		for (var player : players) {
			category.resetSkills(player);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"skills.reset",
				category.getId()
		);
		return players.size();
	}
}
