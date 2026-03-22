package net.puffish.skillsmod.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;

import java.util.concurrent.CompletableFuture;

public class SkillArgumentType implements ArgumentType<String> {

	private static final DynamicCommandExceptionType NO_SUCH_SKILL = new DynamicCommandExceptionType(
			id -> SkillsMod.createTranslatable("command", "no_such_skill", id)
	);

	private final String categoryArgumentName;

	private SkillArgumentType(String categoryArgumentName) {
		this.categoryArgumentName = categoryArgumentName;
	}

	public static SkillArgumentType skillFromCategory(String categoryArgumentName) {
		return new SkillArgumentType(categoryArgumentName);
	}

	public static Skill getSkillFromCategory(CommandContext<CommandSourceStack> context, String name, Category category) throws CommandSyntaxException {
		var skillId = context.getArgument(name, String.class);
		return category.getSkill(skillId).orElseThrow(() -> NO_SUCH_SKILL.create(skillId));
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		var source = context.getSource();
		if (source instanceof CommandSourceStack) {
			var categoryId = SkillsMod.convertIdentifier(context.getArgument(categoryArgumentName, Identifier.class));
			SkillsMod.getInstance()
					.getSkills(categoryId)
					.ifPresent(skills -> SharedSuggestionProvider.suggest(skills, builder));
			return builder.buildFuture();
		} else if (source instanceof SharedSuggestionProvider commandSource) {
			return commandSource.customSuggestion(context);
		}
		return Suggestions.empty();
	}

	public static class Info implements ArgumentTypeInfo<SkillArgumentType, Info.Template> {

		@Override
		public void serializeToNetwork(Template template, FriendlyByteBuf buf) {
			buf.writeUtf(template.categoryArgumentName);
		}

		@Override
		public Template deserializeFromNetwork(FriendlyByteBuf buf) {
			return new Template(buf.readUtf());
		}

		@Override
		public void serializeToJson(Template template, JsonObject jsonObject) {
			jsonObject.addProperty("category_argument_name", template.categoryArgumentName);
		}

		@Override
		public Template unpack(SkillArgumentType skillArgumentType) {
			return new Template(skillArgumentType.categoryArgumentName);
		}

		public final class Template implements ArgumentTypeInfo.Template<SkillArgumentType> {
			private final String categoryArgumentName;

			public Template(String categoryArgumentName) {
				this.categoryArgumentName = categoryArgumentName;
			}

			@Override
			public SkillArgumentType instantiate(CommandBuildContext commandRegistryAccess) {
				return new SkillArgumentType(this.categoryArgumentName);
			}

			@Override
			public ArgumentTypeInfo<SkillArgumentType, ?> type() {
				return Info.this;
			}
		}
	}
}
