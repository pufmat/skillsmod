package net.puffish.skillsmod.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;

import java.util.concurrent.CompletableFuture;

public class SkillArgumentType implements ArgumentType<String> {

	private static final DynamicCommandExceptionType NO_SUCH_SKILL = new DynamicCommandExceptionType(
			id -> SkillsMod.createTranslatable("command", "no_such_skill", id)
	);

	public static SkillArgumentType skill() {
		return new SkillArgumentType();
	}

	public static Skill getSkill(CommandContext<ServerCommandSource> context, String name, Category category) throws CommandSyntaxException {
		var skillId = context.getArgument(name, String.class);
		return category.getSkill(skillId).orElseThrow(() -> NO_SUCH_SKILL.create(skillId));
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		var category = context.getArgument("category", Category.class);
		SkillsMod.getInstance()
				.getSkills(category.getId())
				.ifPresent(skills -> CommandSource.suggestMatching(skills, builder));
		return builder.buildFuture();
	}
}
