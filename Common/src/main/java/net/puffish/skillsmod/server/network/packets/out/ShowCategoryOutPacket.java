package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.GeneralConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.experience.ExperienceConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.server.data.CategoryData;

import java.util.Optional;

public class ShowCategoryOutPacket extends OutPacket {
	public static ShowCategoryOutPacket write(CategoryConfig category, CategoryData categoryData) {
		var packet = new ShowCategoryOutPacket();
		write(packet.buf, category, categoryData);
		return packet;
	}

	public static void write(PacketByteBuf buf, CategoryConfig category, CategoryData categoryData) {
		buf.writeIdentifier(category.getId());
		write(buf, category.getGeneral());
		write(buf, category.getDefinitions());
		write(buf, category.getSkills(), category, categoryData);
		write(buf, category.getConnections());
		buf.writeInt(categoryData.getPointsLeft(category));
		write(buf, category.getExperience(), categoryData);
	}

	public static void write(PacketByteBuf buf, SkillDefinitionsConfig definitions) {
		buf.writeCollection(definitions.getAll(), ShowCategoryOutPacket::write);
	}

	public static void write(PacketByteBuf buf, GeneralConfig general) {
		buf.writeText(general.getTitle());
		write(buf, general.getIcon());
		buf.writeIdentifier(general.getBackground());
		buf.writeBoolean(general.isExclusiveRoot());
	}

	public static void write(PacketByteBuf buf, SkillDefinitionConfig definition) {
		buf.writeString(definition.getId());
		buf.writeText(definition.getTitle());
		buf.writeText(definition.getDescription());
		buf.writeEnumConstant(definition.getFrame());
		write(buf, definition.getIcon());
	}

	public static void write(PacketByteBuf buf, ExperienceConfig experience, CategoryData categoryData) {
		if (experience.isEnabled()) {
			buf.writeOptional(Optional.of(experience.getProgress(categoryData)), PacketByteBuf::writeFloat);
		} else {
			buf.writeOptional(Optional.empty(), PacketByteBuf::writeFloat);
		}
	}

	public static void write(PacketByteBuf buf, SkillsConfig skills, CategoryConfig category, CategoryData categoryData) {
		buf.writeCollection(skills.getAll(), (buf2, skill) -> write(buf2, skill, category, categoryData));
	}

	public static void write(PacketByteBuf buf, SkillConnectionsConfig connections) {
		buf.writeCollection(connections.getAll(), ShowCategoryOutPacket::write);
	}

	public static void write(PacketByteBuf buf, SkillConfig skill, CategoryConfig category, CategoryData categoryData) {
		buf.writeString(skill.getId());
		buf.writeInt(skill.getX());
		buf.writeInt(skill.getY());
		buf.writeString(skill.getDefinitionId());
		buf.writeBoolean(skill.isRoot());
		buf.writeEnumConstant(skill.getStateFor(category, categoryData));
	}

	public static void write(PacketByteBuf buf, SkillConnectionConfig skill) {
		buf.writeString(skill.getSkillAId());
		buf.writeString(skill.getSkillBId());
	}

	public static void write(PacketByteBuf buf, IconConfig icon) {
		buf.writeString(icon.getType());
		buf.writeNullable(icon.getData(), (buf1, element) -> buf1.writeString(element.toString()));
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.SHOW_CATEGORY;
	}
}
