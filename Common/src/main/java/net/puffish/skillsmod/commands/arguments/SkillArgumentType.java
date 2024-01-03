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

	public static Skill getSkillFromCategory(CommandContext<ServerCommandSource> context, String name, Category category) throws CommandSyntaxException {
		var skillId = context.getArgument(name, String.class);
		return category.getSkill(skillId).orElseThrow(() -> NO_SUCH_SKILL.create(skillId));
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		S source = context.getSource();
		if (source instanceof ServerCommandSource) {
			var categoryId = SkillsMod.convertIdentifier(context.getArgument(categoryArgumentName, Identifier.class));
			SkillsMod.getInstance()
					.getSkills(categoryId)
					.ifPresent(skills -> CommandSource.suggestMatching(skills, builder));
			return builder.buildFuture();
		} else if (source instanceof CommandSource commandSource) {
			return commandSource.getCompletions(context);
		}
		return Suggestions.empty();
	}

	public static class Serializer implements ArgumentSerializer<SkillArgumentType, Serializer.Properties> {

		@Override
		public void writePacket(Properties properties, PacketByteBuf buf) {
			buf.writeString(properties.categoryArgumentName);
		}

		@Override
		public Properties fromPacket(PacketByteBuf buf) {
			return new Properties(buf.readString());
		}

		@Override
		public void writeJson(Properties properties, JsonObject jsonObject) {
			jsonObject.addProperty("category_argument_name", properties.categoryArgumentName);
		}

		@Override
		public Properties getArgumentTypeProperties(SkillArgumentType skillArgumentType) {
			return new Properties(skillArgumentType.categoryArgumentName);
		}

		public final class Properties implements ArgumentTypeProperties<SkillArgumentType> {
			private final String categoryArgumentName;

			public Properties(String categoryArgumentName) {
				this.categoryArgumentName = categoryArgumentName;
			}

			@Override
			public SkillArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
				return new SkillArgumentType(this.categoryArgumentName);
			}

			@Override
			public ArgumentSerializer<SkillArgumentType, ?> getSerializer() {
				return Serializer.this;
			}
		}
	}
}
