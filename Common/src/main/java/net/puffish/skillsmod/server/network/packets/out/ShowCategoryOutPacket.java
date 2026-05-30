package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.common.FrameType;
import net.puffish.skillsmod.common.IconType;
import net.puffish.skillsmod.common.SkillConnection;
import net.puffish.skillsmod.config.BackgroundConfig;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.FrameConfig;
import net.puffish.skillsmod.config.GeneralConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.colors.ColorConfig;
import net.puffish.skillsmod.config.colors.ColorsConfig;
import net.puffish.skillsmod.config.colors.ConnectionsColorsConfig;
import net.puffish.skillsmod.config.colors.FillStrokeColorsConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.server.data.CategoryData;

public record ShowCategoryOutPacket(CategoryConfig category, CategoryData categoryData) implements OutPacket {

	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeIdentifier(category.id());
		buf.writeInt(category.position());
		write(buf, category.general());
		write(buf, category.definitions());
		write(buf, category.skills());
		write(buf, category.connections());
		buf.writeMap(
				category.skills().getMap(),
				FriendlyByteBuf::writeUtf,
				(buf1, skill) -> buf1.writeEnum(
						categoryData.getSkillState(
								category,
								skill,
								category.definitions().getById(skill.definitionId()).orElseThrow()
						)
				)
		);
		buf.writeInt(categoryData.getSpentPoints(category));
		buf.writeInt(categoryData.getPointsTotal());
		category.experience().ifPresentOrElse(experience -> {
			buf.writeBoolean(true);
			var curve = experience.curve();
			buf.writeInt(curve.getLevelLimit());
			var progress = curve.getProgress(categoryData.getExperience());
			buf.writeInt(progress.currentLevel());
			buf.writeInt(progress.currentExperience());
			buf.writeInt(progress.requiredExperience());
		}, () -> buf.writeBoolean(false));
	}

	public void write(RegistryFriendlyByteBuf buf, SkillDefinitionsConfig definitions) {
		buf.writeCollection(definitions.getAll(), (buf1, definition) -> write(buf, definition));
	}

	public void write(RegistryFriendlyByteBuf buf, GeneralConfig general) {
		ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, general.title());
		ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, general.description());
		ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, general.extraDescription());
		write(buf, general.icon());
		write(buf, general.background());
		write(buf, general.colors());
		buf.writeBoolean(general.exclusiveRoot());
		buf.writeInt(general.spentPointsLimit());
	}

	public void write(RegistryFriendlyByteBuf buf, SkillDefinitionConfig definition) {
		buf.writeUtf(definition.id());
		ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, definition.title());
		ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, definition.description());
		ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, definition.extraDescription());
		write(buf, definition.frame());
		write(buf, definition.icon());
		buf.writeFloat(definition.size());
		buf.writeInt(definition.cost());
		buf.writeInt(definition.requiredSkills());
		buf.writeInt(definition.requiredPoints());
		buf.writeInt(definition.requiredSpentPoints());
		buf.writeInt(definition.requiredExclusions());
	}

	public void write(FriendlyByteBuf buf, SkillsConfig skills) {
		buf.writeCollection(skills.getAll(), ShowCategoryOutPacket::write);
	}

	public void write(FriendlyByteBuf buf, SkillConnectionsConfig connections) {
		buf.writeCollection(connections.normal().getAll(), ShowCategoryOutPacket::write);
		buf.writeCollection(connections.exclusive().getAll(), ShowCategoryOutPacket::write);
	}

	public static void write(FriendlyByteBuf buf, SkillConfig skill) {
		buf.writeUtf(skill.id());
		buf.writeInt(skill.x());
		buf.writeInt(skill.y());
		buf.writeUtf(skill.definitionId());
		buf.writeBoolean(skill.isRoot());
	}

	public static void write(FriendlyByteBuf buf, SkillConnection skill) {
		buf.writeUtf(skill.skillAId());
		buf.writeUtf(skill.skillBId());
		buf.writeBoolean(skill.bidirectional());
	}

	public static void write(RegistryFriendlyByteBuf buf, IconConfig icon) {
		if (icon instanceof IconConfig.EffectIconConfig effectIcon) {
			buf.writeEnum(IconType.EFFECT);
			buf.writeIdentifier(BuiltInRegistries.MOB_EFFECT.getKey(effectIcon.effect()));
		} else if (icon instanceof IconConfig.ItemIconConfig itemIcon) {
			buf.writeEnum(IconType.ITEM);
			ItemStack.STREAM_CODEC.encode(buf, itemIcon.item());
		} else if (icon instanceof IconConfig.TextureIconConfig textureIcon) {
			buf.writeEnum(IconType.TEXTURE);
			buf.writeIdentifier(textureIcon.texture());
		}
	}

	public static void write(FriendlyByteBuf buf, FrameConfig frame) {
		if (frame instanceof FrameConfig.AdvancementFrameConfig advancementFrame) {
			buf.writeEnum(FrameType.ADVANCEMENT);
			buf.writeEnum(advancementFrame.frame());
		} else if (frame instanceof FrameConfig.TextureFrameConfig textureFrame) {
			buf.writeEnum(FrameType.TEXTURE);
			buf.writeOptional(textureFrame.lockedTexture(), FriendlyByteBuf::writeIdentifier);
			buf.writeIdentifier(textureFrame.availableTexture());
			buf.writeOptional(textureFrame.affordableTexture(), FriendlyByteBuf::writeIdentifier);
			buf.writeIdentifier(textureFrame.unlockedTexture());
			buf.writeOptional(textureFrame.excludedTexture(), FriendlyByteBuf::writeIdentifier);
		}
	}

	public static void write(FriendlyByteBuf buf, BackgroundConfig background) {
		buf.writeIdentifier(background.texture());
		buf.writeInt(background.width());
		buf.writeInt(background.height());
		buf.writeEnum(background.position());
	}

	public static void write(FriendlyByteBuf buf, ColorsConfig colors) {
		write(buf, colors.connections());
		write(buf, colors.points());
	}

	public static void write(FriendlyByteBuf buf, ConnectionsColorsConfig connectionsColors) {
		write(buf, connectionsColors.locked());
		write(buf, connectionsColors.available());
		write(buf, connectionsColors.affordable());
		write(buf, connectionsColors.unlocked());
		write(buf, connectionsColors.excluded());
	}

	public static void write(FriendlyByteBuf buf, FillStrokeColorsConfig fillStrokeColors) {
		write(buf, fillStrokeColors.fill());
		write(buf, fillStrokeColors.stroke());
	}

	public static void write(FriendlyByteBuf buf, ColorConfig color) {
		buf.writeInt(color.argb());
	}

	@Override
	public Identifier getId() {
		return Packets.SHOW_CATEGORY;
	}
}
