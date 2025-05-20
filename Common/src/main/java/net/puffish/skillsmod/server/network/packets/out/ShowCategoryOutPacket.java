package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
	public void write(PacketByteBuf buf) {
		buf.writeIdentifier(category.id());
		write(buf, category.general());
		write(buf, category.definitions());
		write(buf, category.skills());
		write(buf, category.connections());
		buf.writeMap(
				category.skills().getMap(),
				PacketByteBuf::writeString,
				(buf1, skill) -> buf1.writeEnumConstant(
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

	public void write(PacketByteBuf buf, SkillDefinitionsConfig definitions) {
		buf.writeCollection(definitions.getAll(), (buf1, definition) -> write(buf, definition));
	}

	public void write(PacketByteBuf buf, GeneralConfig general) {
		buf.writeText(general.title());
		write(buf, general.icon());
		write(buf, general.background());
		write(buf, general.colors());
		buf.writeBoolean(general.exclusiveRoot());
		buf.writeInt(general.spentPointsLimit());
	}

	public void write(PacketByteBuf buf, SkillDefinitionConfig definition) {
		buf.writeString(definition.id());
		buf.writeText(definition.title());
		buf.writeText(definition.description());
		buf.writeText(definition.extraDescription());
		write(buf, definition.frame());
		write(buf, definition.icon());
		buf.writeFloat(definition.size());
		buf.writeInt(definition.cost());
		buf.writeInt(definition.requiredSkills());
		buf.writeInt(definition.requiredPoints());
		buf.writeInt(definition.requiredSpentPoints());
		buf.writeInt(definition.requiredExclusions());
	}

	public void write(PacketByteBuf buf, SkillsConfig skills) {
		buf.writeCollection(skills.getAll(), ShowCategoryOutPacket::write);
	}

	public void write(PacketByteBuf buf, SkillConnectionsConfig connections) {
		buf.writeCollection(connections.normal().getAll(), ShowCategoryOutPacket::write);
		buf.writeCollection(connections.exclusive().getAll(), ShowCategoryOutPacket::write);
	}

	public static void write(PacketByteBuf buf, SkillConfig skill) {
		buf.writeString(skill.id());
		buf.writeInt(skill.x());
		buf.writeInt(skill.y());
		buf.writeString(skill.definitionId());
		buf.writeBoolean(skill.isRoot());
	}

	public static void write(PacketByteBuf buf, SkillConnection skill) {
		buf.writeString(skill.skillAId());
		buf.writeString(skill.skillBId());
		buf.writeBoolean(skill.bidirectional());
	}

	public static void write(PacketByteBuf buf, IconConfig icon) {
		if (icon instanceof IconConfig.EffectIconConfig effectIcon) {
			buf.writeEnumConstant(IconType.EFFECT);
			buf.writeIdentifier(Registries.STATUS_EFFECT.getId(effectIcon.effect()));
		} else if (icon instanceof IconConfig.ItemIconConfig itemIcon) {
			buf.writeEnumConstant(IconType.ITEM);
			buf.writeItemStack(itemIcon.item());
		} else if (icon instanceof IconConfig.TextureIconConfig textureIcon) {
			buf.writeEnumConstant(IconType.TEXTURE);
			buf.writeIdentifier(textureIcon.texture());
		}
	}

	public static void write(PacketByteBuf buf, FrameConfig frame) {
		if (frame instanceof FrameConfig.AdvancementFrameConfig advancementFrame) {
			buf.writeEnumConstant(FrameType.ADVANCEMENT);
			buf.writeEnumConstant(advancementFrame.frame());
		} else if (frame instanceof FrameConfig.TextureFrameConfig textureFrame) {
			buf.writeEnumConstant(FrameType.TEXTURE);
			buf.writeOptional(textureFrame.lockedTexture(), PacketByteBuf::writeIdentifier);
			buf.writeIdentifier(textureFrame.availableTexture());
			buf.writeOptional(textureFrame.affordableTexture(), PacketByteBuf::writeIdentifier);
			buf.writeIdentifier(textureFrame.unlockedTexture());
			buf.writeOptional(textureFrame.excludedTexture(), PacketByteBuf::writeIdentifier);
		}
	}

	public static void write(PacketByteBuf buf, BackgroundConfig background) {
		buf.writeIdentifier(background.texture());
		buf.writeInt(background.width());
		buf.writeInt(background.height());
		buf.writeEnumConstant(background.position());
	}

	public static void write(PacketByteBuf buf, ColorsConfig colors) {
		write(buf, colors.connections());
		write(buf, colors.points());
	}

	public static void write(PacketByteBuf buf, ConnectionsColorsConfig connectionsColors) {
		write(buf, connectionsColors.locked());
		write(buf, connectionsColors.available());
		write(buf, connectionsColors.affordable());
		write(buf, connectionsColors.unlocked());
		write(buf, connectionsColors.excluded());
	}

	public static void write(PacketByteBuf buf, FillStrokeColorsConfig fillStrokeColors) {
		write(buf, fillStrokeColors.fill());
		write(buf, fillStrokeColors.stroke());
	}

	public static void write(PacketByteBuf buf, ColorConfig color) {
		buf.writeInt(color.argb());
	}

	@Override
	public Identifier getId() {
		return Packets.SHOW_CATEGORY;
	}
}
