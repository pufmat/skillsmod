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
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.utils.CommandUtils;

import java.util.concurrent.CompletableFuture;

public class CategoryArgumentType implements ArgumentType<Category> {

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

	public static Category getCategory(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
		return context.getArgument(name, Category.class);
	}

	@Override
	public Category parse(StringReader reader) throws CommandSyntaxException {
		var categoryId = SkillsMod.convertIdentifier(Identifier.fromCommandInput(reader));
		return SkillsAPI.getCategory(categoryId)
				.filter(category -> !onlyWithExperience || category.getExperience().isPresent())
				.orElseThrow(() -> NO_SUCH_CATEGORY.create(categoryId));
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		CommandUtils.suggestIdentifiers(SkillsMod.getInstance().getCategories(onlyWithExperience), builder);
		return builder.buildFuture();
	}

	public static class Serializer implements ArgumentSerializer<CategoryArgumentType, Serializer.Properties> {

		@Override
		public void writePacket(CategoryArgumentType.Serializer.Properties properties, PacketByteBuf buf) {
			buf.writeBoolean(properties.onlyWithExperience());
		}

		@Override
		public CategoryArgumentType.Serializer.Properties fromPacket(PacketByteBuf buf) {
			return new CategoryArgumentType.Serializer.Properties(buf.readBoolean());
		}

		@Override
		public void writeJson(CategoryArgumentType.Serializer.Properties properties, JsonObject jsonObject) {
			jsonObject.addProperty("only_with_experience", properties.onlyWithExperience());
		}

		@Override
		public CategoryArgumentType.Serializer.Properties getArgumentTypeProperties(CategoryArgumentType categoryArgumentType) {
			return new CategoryArgumentType.Serializer.Properties(categoryArgumentType.onlyWithExperience);
		}

		public record Properties(boolean onlyWithExperience) implements ArgumentSerializer.ArgumentTypeProperties<CategoryArgumentType> {

			@Override
			public CategoryArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
				return new CategoryArgumentType(this.onlyWithExperience);
			}

			@Override
			public ArgumentSerializer<CategoryArgumentType, ?> getSerializer() {
				return new CategoryArgumentType.Serializer();
			}
		}
	}
}
