package net.puffish.skillsmod.utils;

import com.google.common.collect.ObjectArrays;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Collection;
import java.util.Locale;

public class CommandUtils {

	public static int sendSuccess(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> players, String command, Object... args) {
		if (players.size() == 1) {
			context.getSource().sendFeedback(() -> SkillsMod.createTranslatable(
					"command", command + ".success.single", ObjectArrays.concat(args, players.iterator().next().getDisplayName())
			), true);
		} else {
			context.getSource().sendFeedback(() -> SkillsMod.createTranslatable(
					"command", command + ".success.multiple", ObjectArrays.concat(args, players.size())
			), true);
		}
		return players.size();
	}

	public static void suggestIdentifiers(Iterable<Identifier> ids, SuggestionsBuilder builder) {
		var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
		var hasColon = remaining.indexOf(':') != -1;
		for (var id : ids) {
			if (hasColon) {
				if (CommandSource.shouldSuggest(remaining, id.toString())) {
					builder.suggest(id.toString());
				}
			} else if (CommandSource.shouldSuggest(remaining, id.getNamespace())) {
				builder.suggest(id.toString());
			} else if (id.getNamespace().equals(SkillsAPI.MOD_ID)) {
				if (CommandSource.shouldSuggest(remaining, id.getPath())) {
					builder.suggest(id.toString());
				}
			}
		}
	}
}
