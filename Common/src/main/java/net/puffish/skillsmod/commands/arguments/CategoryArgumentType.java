package net.puffish.skillsmod.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
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

	public static Category getCategory(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
		var categoryId = SkillsMod.convertIdentifier(context.getArgument(name, Identifier.class));
		return SkillsAPI.getCategory(categoryId)
				.orElseThrow(() -> NO_SUCH_CATEGORY.create(categoryId));
	}

	public static Category getCategoryOnlyWithExperience(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
		var categoryId = SkillsMod.convertIdentifier(context.getArgument(name, Identifier.class));
		var category = SkillsAPI.getCategory(categoryId)
				.orElseThrow(() -> NO_SUCH_CATEGORY.create(categoryId));
		if (category.getExperience().isEmpty()) {
			throw NO_EXPERIENCE.create(categoryId);
		}
		return category;
	}

	public static Category getCategoryOnlyWithLevel(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
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
		return Identifier.fromCommandInput(reader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		var source = context.getSource();
		if (source instanceof ServerCommandSource) {
			CommandUtils.suggestIdentifiers(SkillsMod.getInstance().getCategories(filter), builder);
			return builder.buildFuture();
		} else if (source instanceof CommandSource commandSource) {
			return commandSource.getCompletions(context);
		}
		return Suggestions.empty();
	}

	public static class Serializer implements ArgumentSerializer<CategoryArgumentType, Serializer.Properties> {

		@Override
		public void writePacket(Properties properties, PacketByteBuf buf) {
			buf.writeEnumConstant(properties.filter);
		}

		@Override
		public Properties fromPacket(PacketByteBuf buf) {
			return new Properties(buf.readEnumConstant(CategoryFilter.class));
		}

		@Override
		public void writeJson(Properties properties, JsonObject jsonObject) {
			jsonObject.addProperty("filter", properties.filter.name().toLowerCase());
		}

		@Override
		public Properties getArgumentTypeProperties(CategoryArgumentType categoryArgumentType) {
			return new Properties(categoryArgumentType.filter);
		}

		public final class Properties implements ArgumentTypeProperties<CategoryArgumentType> {
			private final CategoryFilter filter;

			public Properties(CategoryFilter filter) {
				this.filter = filter;
			}

			@Override
			public CategoryArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
				return new CategoryArgumentType(this.filter);
			}

			@Override
			public ArgumentSerializer<CategoryArgumentType, ?> getSerializer() {
				return Serializer.this;
			}
		}
	}
}
