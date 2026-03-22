package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.config.ClientBackgroundConfig;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.ClientFrameConfig;
import net.puffish.skillsmod.client.config.ClientIconConfig;
import net.puffish.skillsmod.client.config.colors.ClientColorConfig;
import net.puffish.skillsmod.client.config.colors.ClientColorsConfig;
import net.puffish.skillsmod.client.config.colors.ClientConnectionsColorsConfig;
import net.puffish.skillsmod.client.config.colors.ClientFillStrokeColorsConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConnectionConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.common.BackgroundPosition;
import net.puffish.skillsmod.common.FrameType;
import net.puffish.skillsmod.common.IconType;
import net.puffish.skillsmod.network.InPacket;

import java.util.stream.Collectors;

public class ShowCategoryInPacket implements InPacket {
	private final ClientCategoryData category;

	private ShowCategoryInPacket(ClientCategoryData category) {
		this.category = category;
	}

	public static ShowCategoryInPacket read(RegistryFriendlyByteBuf buf) {
		var category = readCategory(buf);

		return new ShowCategoryInPacket(category);
	}

	public static ClientCategoryData readCategory(RegistryFriendlyByteBuf buf) {
		var id = buf.readIdentifier();

		var title = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var description = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var extraDescription = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var icon = readSkillIcon(buf);
		var background = readBackground(buf);
		var colors = readColors(buf);
		var exclusiveRoot = buf.readBoolean();
		var spentPointsLimit = buf.readInt();

		var definitions = buf.readList(buf1 -> ShowCategoryInPacket.readDefinition(buf))
				.stream()
				.collect(Collectors.toMap(ClientSkillDefinitionConfig::id, definition -> definition));

		var skills = buf.readList(ShowCategoryInPacket::readSkill)
				.stream()
				.collect(Collectors.toMap(ClientSkillConfig::id, skill -> skill));

		var normalConnections = buf.readList(ShowCategoryInPacket::readSkillConnection);
		var exclusiveConnections = buf.readList(ShowCategoryInPacket::readSkillConnection);

		var skillsStates = buf.readMap(
				FriendlyByteBuf::readUtf,
				buf1 -> buf1.readEnum(Skill.State.class)
		);

		var spentPoints = buf.readInt();
		var earnedPoints = buf.readInt();

		var levelLimit = Integer.MAX_VALUE;
		var currentLevel = Integer.MIN_VALUE;
		var currentExperience = Integer.MIN_VALUE;
		var requiredExperience = Integer.MIN_VALUE;
		if (buf.readBoolean()) {
			levelLimit = buf.readInt();
			currentLevel = buf.readInt();
			currentExperience = buf.readInt();
			requiredExperience = buf.readInt();
		}

		var category = new ClientCategoryConfig(
				id,
				title,
				description,
				extraDescription,
				icon,
				background,
				colors,
				exclusiveRoot,
				spentPointsLimit,
				levelLimit,
				definitions,
				skills,
				normalConnections,
				exclusiveConnections
		);

		return new ClientCategoryData(
				category,
				skillsStates,
				spentPoints,
				earnedPoints,
				currentLevel,
				currentExperience,
				requiredExperience
		);
	}

	public static ClientSkillDefinitionConfig readDefinition(RegistryFriendlyByteBuf buf) {
		var id = buf.readUtf();
		var title = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var description = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var extraDescription = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var frame = readFrameIcon(buf);
		var icon = readSkillIcon(buf);
		var size = buf.readFloat();
		var cost = buf.readInt();
		var requiredSkills = buf.readInt();
		var requiredPoints = buf.readInt();
		var requiredSpentPoints = buf.readInt();
		var requiredExclusions = buf.readInt();

		return new ClientSkillDefinitionConfig(
				id,
				title,
				description,
				extraDescription,
				icon,
				frame,
				size,
				cost,
				requiredSkills,
				requiredPoints,
				requiredSpentPoints,
				requiredExclusions
		);
	}

	public static ClientIconConfig readSkillIcon(RegistryFriendlyByteBuf buf) {
		var type = buf.readEnum(IconType.class);
		return switch (type) {
			case EFFECT -> {
				var effect = buf.readIdentifier();
				yield new ClientIconConfig.EffectIconConfig(BuiltInRegistries.MOB_EFFECT.getValue(effect));
			}
			case ITEM -> {
				var itemStack = ItemStack.STREAM_CODEC.decode(buf);
				yield new ClientIconConfig.ItemIconConfig(itemStack);
			}
			case TEXTURE -> {
				var texture = buf.readIdentifier();
				yield new ClientIconConfig.TextureIconConfig(texture);
			}
		};
	}

	public static ClientFrameConfig readFrameIcon(FriendlyByteBuf buf) {
		var type = buf.readEnum(FrameType.class);
		return switch (type) {
			case ADVANCEMENT -> {
				var advancementFrame = buf.readEnum(AdvancementType.class);
				yield new ClientFrameConfig.AdvancementFrameConfig(advancementFrame);
			}
			case TEXTURE -> {
				var lockedTexture = buf.readOptional(FriendlyByteBuf::readIdentifier);
				var availableTexture = buf.readIdentifier();
				var affordableTexture = buf.readOptional(FriendlyByteBuf::readIdentifier);
				var unlockedTexture = buf.readIdentifier();
				var excludedTexture = buf.readOptional(FriendlyByteBuf::readIdentifier);
				yield new ClientFrameConfig.TextureFrameConfig(
						lockedTexture,
						availableTexture,
						affordableTexture,
						unlockedTexture,
						excludedTexture
				);
			}
		};
	}

	public static ClientBackgroundConfig readBackground(FriendlyByteBuf buf) {
		var texture = buf.readIdentifier();
		var width = buf.readInt();
		var height = buf.readInt();
		var position = buf.readEnum(BackgroundPosition.class);

		return ClientBackgroundConfig.create(texture, width, height, position);
	}

	public static ClientColorsConfig readColors(FriendlyByteBuf buf) {
		var connections = readConnectionsColors(buf);
		var points = readFillStrokeColors(buf);

		return new ClientColorsConfig(connections, points);
	}

	public static ClientConnectionsColorsConfig readConnectionsColors(FriendlyByteBuf buf) {
		var locked = readFillStrokeColors(buf);
		var available = readFillStrokeColors(buf);
		var affordable = readFillStrokeColors(buf);
		var unlocked = readFillStrokeColors(buf);
		var excluded = readFillStrokeColors(buf);

		return new ClientConnectionsColorsConfig(locked, available, affordable, unlocked, excluded);
	}

	public static ClientFillStrokeColorsConfig readFillStrokeColors(FriendlyByteBuf buf) {
		var fill = readColor(buf);
		var stroke = readColor(buf);

		return new ClientFillStrokeColorsConfig(fill, stroke);
	}

	public static ClientColorConfig readColor(FriendlyByteBuf buf) {
		var argb = buf.readInt();

		return new ClientColorConfig(argb);
	}

	public static ClientSkillConfig readSkill(FriendlyByteBuf buf) {
		var id = buf.readUtf();
		var x = buf.readInt();
		var y = buf.readInt();
		var definition = buf.readUtf();
		var isRoot = buf.readBoolean();

		return new ClientSkillConfig(id, x, y, definition, isRoot);
	}

	public static ClientSkillConnectionConfig readSkillConnection(FriendlyByteBuf buf) {
		var skillAId = buf.readUtf();
		var skillBId = buf.readUtf();
		var bidirectional = buf.readBoolean();

		return new ClientSkillConnectionConfig(skillAId, skillBId, bidirectional);
	}

	public ClientCategoryData getCategory() {
		return category;
	}
}
