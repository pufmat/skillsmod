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
import net.puffish.skillsmod.util.CategoryFilter;
import net.puffish.skillsmod.util.CommandUtils;

import java.util.concurrent.CompletableFuture;

public class CategoryArgumentType implements ArgumentType<Identifier> {

	private static final DynamicCommandExceptionType NO_SUCH_CATEGORY = new DynamicCommandExceptionType(
			id -> SkillsMod.createTranslatable("command", "no_such_category", id)
	);

	private static final DynamicCommandExceptionType NO_EXPERIENCE = new DynamicCommandExceptionType(
			id -> SkillsMod.createTranslatable("command", "no_experience", id)
	);

	private static final DynamicCommandExceptionType NO_LEVEL = new DynamicCommandExceptionType(
			id -> SkillsMod.createTranslatable("command", "no_level", id)
	);

	private final CategoryFilter filter;

	public CategoryArgumentType(CategoryFilter filter) {
		this.filter = filter;
	}

	public static CategoryArgumentType category() {
		return new CategoryArgumentType(CategoryFilter.ALL);
	}

	public static CategoryArgumentType categoryOnlyWithExperience() {
		return new CategoryArgumentType(CategoryFilter.WITH_EXPERIENCE);
	}

	public static CategoryArgumentType categoryOnlyWithLevel() {
		return new CategoryArgumentType(CategoryFilter.WITH_LEVEL);
	}

	public static Category getCategory(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		var categoryId = SkillsMod.convertIdentifier(context.getArgument(name, Identifier.class));
		return SkillsAPI.getCategory(categoryId)
				.orElseThrow(() -> NO_SUCH_CATEGORY.create(categoryId));
	}

	public static Category getCategoryOnlyWithExperience(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		var categoryId = SkillsMod.convertIdentifier(context.getArgument(name, Identifier.class));
		var category = SkillsAPI.getCategory(categoryId)
				.orElseThrow(() -> NO_SUCH_CATEGORY.create(categoryId));
		if (category.getExperience().isEmpty()) {
			throw NO_EXPERIENCE.create(categoryId);
		}
		return category;
	}

	public static Category getCategoryOnlyWithLevel(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		var categoryId = SkillsMod.convertIdentifier(context.getArgument(name, Identifier.class));
		var category = SkillsAPI.getCategory(categoryId)
				.orElseThrow(() -> NO_SUCH_CATEGORY.create(categoryId));
		if (category.getExperience().isEmpty() && category.getExchange().isEmpty()) {
			throw NO_LEVEL.create(categoryId);
		}
		return category;
	}

	@Override
	public Identifier parse(StringReader reader) throws CommandSyntaxException {
		return Identifier.read(reader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		var source = context.getSource();
		if (source instanceof CommandSourceStack) {
			CommandUtils.suggestIdentifiers(SkillsMod.getInstance().getCategories(filter), builder);
			return builder.buildFuture();
		} else if (source instanceof SharedSuggestionProvider commandSource) {
			return commandSource.customSuggestion(context);
		}
		return Suggestions.empty();
	}

	public static class Info implements ArgumentTypeInfo<CategoryArgumentType, Info.Template> {

		@Override
		public void serializeToNetwork(Template template, FriendlyByteBuf buf) {
			buf.writeEnum(template.filter);
		}

		@Override
		public Template deserializeFromNetwork(FriendlyByteBuf buf) {
			return new Template(buf.readEnum(CategoryFilter.class));
		}

		@Override
		public void serializeToJson(Template template, JsonObject jsonObject) {
			jsonObject.addProperty("filter", template.filter.name().toLowerCase());
		}

		@Override
		public Template unpack(CategoryArgumentType categoryArgumentType) {
			return new Template(categoryArgumentType.filter);
		}

		public final class Template implements ArgumentTypeInfo.Template<CategoryArgumentType> {
			private final CategoryFilter filter;

			public Template(CategoryFilter filter) {
				this.filter = filter;
			}

			@Override
			public CategoryArgumentType instantiate(CommandBuildContext commandBuildContext) {
				return new CategoryArgumentType(this.filter);
			}

			@Override
			public ArgumentTypeInfo<CategoryArgumentType, ?> type() {
				return Info.this;
			}
		}
	}
}
