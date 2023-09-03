package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.GeneralConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.skill.SkillConnection;

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
		buf.writeInt(categoryData.getSpentPointsLimit(category));
		write(buf, category.getDefinitions());
		write(buf, category.getSkills(), category, categoryData);
		write(buf, category.getConnections());
		buf.writeInt(categoryData.getSpentPoints(category));
		buf.writeInt(categoryData.getEarnedPoints(category));
		if (category.getExperience().isPresent()) {
			buf.writeBoolean(true);
			buf.writeInt(categoryData.getCurrentLevel(category));
			buf.writeInt(categoryData.getCurrentExperience(category));
			buf.writeInt(categoryData.getRequiredExperience(category));
		} else {
			buf.writeBoolean(false);
		}
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

	public static void write(PacketByteBuf buf, SkillsConfig skills, CategoryConfig category, CategoryData categoryData) {
		buf.writeCollection(skills.getAll(), (buf2, skill) -> write(buf2, skill, category, categoryData));
	}

	public static void write(PacketByteBuf buf, SkillConnectionsConfig connections) {
		buf.writeCollection(connections.getNormal().getAll(), ShowCategoryOutPacket::write);
		buf.writeCollection(connections.getExclusive().getAll(), ShowCategoryOutPacket::write);
	}

	public static void write(PacketByteBuf buf, SkillConfig skill, CategoryConfig category, CategoryData categoryData) {
		buf.writeString(skill.getId());
		buf.writeInt(skill.getX());
		buf.writeInt(skill.getY());
		buf.writeString(skill.getDefinitionId());
		buf.writeBoolean(skill.isRoot());
		buf.writeEnumConstant(categoryData.getSkillState(category, skill));
	}

	public static void write(PacketByteBuf buf, SkillConnection skill) {
		buf.writeString(skill.skillAId());
		buf.writeString(skill.skillBId());
		buf.writeBoolean(skill.bidirectional());
	}

	public static void write(PacketByteBuf buf, IconConfig icon) {
		buf.writeString(icon.getType());
		buf.writeOptional(Optional.ofNullable(icon.getData()), (buf1, element) -> buf1.writeString(element.toString()));
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.SHOW_CATEGORY;
	}
}
