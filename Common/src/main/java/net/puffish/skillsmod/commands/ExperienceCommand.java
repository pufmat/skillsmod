package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.util.CommandUtils;

public class ExperienceCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> create() {
		return Commands.literal("experience")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("add")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.categoryOnlyWithExperience())
										.then(Commands.argument("amount", IntegerArgumentType.integer())
												.executes(ExperienceCommand::add)
										)
								)
						)
				)
				.then(Commands.literal("set")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.categoryOnlyWithExperience())
										.then(Commands.argument("amount", IntegerArgumentType.integer())
												.executes(ExperienceCommand::set)
										)
								)
						)
				)
				.then(Commands.literal("get")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("category", CategoryArgumentType.categoryOnlyWithExperience())
										.executes(ExperienceCommand::get)
								)
						)
				);
	}

	private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
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

	private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
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

	private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var player = EntityArgument.getPlayer(context, "player");
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
