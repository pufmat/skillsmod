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
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.util.CommandUtils;

import java.util.concurrent.CompletableFuture;

public class CategoryArgumentType implements ArgumentType<Identifier> {

	private static final DynamicCommandExceptionType NO_SUCH_CATEGORY = new DynamicCommandExceptionType(
			id -> SkillsMod.createTranslatable("command", "no_such_category", id)
	);

	private final boolean onlyWithExperience;

	public CategoryArgumentType(boolean onlyWithExperience) {
		this.onlyWithExperience = onlyWithExperience;
	}

	public static CategoryArgumentType category() {
		return new CategoryArgumentType(false);
	}

	public static CategoryArgumentType categoryOnlyWithExperience() {
		return new CategoryArgumentType(true);
	}

	public static Category getCategory(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		var categoryId = SkillsMod.convertIdentifier(context.getArgument(name, Identifier.class));
		return SkillsAPI.getCategory(categoryId)
				.orElseThrow(() -> NO_SUCH_CATEGORY.create(categoryId));
	}

	public static Category getCategoryOnlyWithExperience(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		var categoryId = SkillsMod.convertIdentifier(context.getArgument(name, Identifier.class));
		return SkillsAPI.getCategory(categoryId)
				.filter(category -> category.getExperience().isPresent())
				.orElseThrow(() -> NO_SUCH_CATEGORY.create(categoryId));
	}

	@Override
	public Identifier parse(StringReader reader) throws CommandSyntaxException {
		return Identifier.read(reader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		var source = context.getSource();
		if (source instanceof CommandSourceStack) {
			CommandUtils.suggestIdentifiers(SkillsMod.getInstance().getCategories(onlyWithExperience), builder);
			return builder.buildFuture();
		} else if (source instanceof SharedSuggestionProvider commandSource) {
			return commandSource.customSuggestion(context);
		}
		return Suggestions.empty();
	}

	public static class Info implements ArgumentTypeInfo<CategoryArgumentType, Info.Template> {

		@Override
		public void serializeToNetwork(Template template, FriendlyByteBuf buf) {
			buf.writeBoolean(template.onlyWithExperience);
		}

		@Override
		public Template deserializeFromNetwork(FriendlyByteBuf buf) {
			return new Template(buf.readBoolean());
		}

		@Override
		public void serializeToJson(Template template, JsonObject jsonObject) {
			jsonObject.addProperty("only_with_experience", template.onlyWithExperience);
		}

		@Override
		public Template unpack(CategoryArgumentType categoryArgumentType) {
			return new Template(categoryArgumentType.onlyWithExperience);
		}

		public final class Template implements ArgumentTypeInfo.Template<CategoryArgumentType> {
			private final boolean onlyWithExperience;

			public Template(boolean onlyWithExperience) {
				this.onlyWithExperience = onlyWithExperience;
			}

			@Override
			public CategoryArgumentType instantiate(CommandBuildContext commandBuildContext) {
				return new CategoryArgumentType(this.onlyWithExperience);
			}

			@Override
			public ArgumentTypeInfo<CategoryArgumentType, ?> type() {
				return Info.this;
			}
		}
	}
}
