package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.util.CommandUtils;

public class ExperienceCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("experience")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("add")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.categoryOnlyWithExperience())
										.then(CommandManager.argument("amount", IntegerArgumentType.integer())
												.executes(ExperienceCommand::add)
										)
								)
						)
				)
				.then(CommandManager.literal("set")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.categoryOnlyWithExperience())
										.then(CommandManager.argument("amount", IntegerArgumentType.integer())
												.executes(ExperienceCommand::set)
										)
								)
						)
				)
				.then(CommandManager.literal("get")
						.then(CommandManager.argument("player", EntityArgumentType.player())
								.then(CommandManager.argument("category", CategoryArgumentType.categoryOnlyWithExperience())
										.executes(ExperienceCommand::get)
								)
						)
				);
	}

	private static int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var players = EntityArgumentType.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategoryOnlyWithExperience(context, "category");
		var amount = IntegerArgumentType.getInteger(context, "amount");

		var experience = category.getExperience().orElseThrow();

		for (var player : players) {
			experience.addTotal(player, amount);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"experience.add",
				amount,
				category.getId()
		);
		return players.size();
	}

	private static int set(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var players = EntityArgumentType.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategoryOnlyWithExperience(context, "category");
		var amount = IntegerArgumentType.getInteger(context, "amount");

		var experience = category.getExperience().orElseThrow();

		for (var player : players) {
			experience.setTotal(player, amount);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"experience.set",
				amount,
				category.getId()
		);
		return players.size();
	}

	private static int get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var player = EntityArgumentType.getPlayer(context, "player");
		var category = CategoryArgumentType.getCategoryOnlyWithExperience(context, "category");

		var experience = category.getExperience().orElseThrow();

		var amount = experience.getTotal(player);
		CommandUtils.sendSuccess(
				context,
				player,
				"experience.get",
				amount,
				category.getId()
		);
		return amount;
	}
}
