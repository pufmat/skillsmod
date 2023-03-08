package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.GeneralConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.config.experience.ExperienceConfig;
import net.puffish.skillsmod.network.Packets;

import java.util.Optional;

public class ShowCategoryOutPacket extends OutPacket {
	public ShowCategoryOutPacket(CategoryConfig category, CategoryData categoryData) {
		write(category, categoryData);
	}

	private void write(CategoryConfig category, CategoryData categoryData) {
		buf.writeString(category.getId());
		buf.writeInt(category.getIndex());
		write(category.getGeneral());
		write(category.getDefinitions());
		write(category.getSkills(), category, categoryData);
		write(category.getConnections());
		write(category.getExperience(), categoryData);
	}

	private void write(SkillDefinitionsConfig definitions) {
		buf.writeCollection(definitions.getAll(), (buf2, definition) -> write(definition));
	}

	private void write(GeneralConfig general) {
		buf.writeText(general.getTitle());
		write(general.getIcon());
		buf.writeIdentifier(general.getBackground());
	}

	private void write(SkillDefinitionConfig definition) {
		buf.writeString(definition.getId());
		buf.writeText(definition.getTitle());
		buf.writeText(definition.getDescription());
		buf.writeEnumConstant(definition.getFrame());
		write(definition.getIcon());
	}

	private void write(ExperienceConfig experience, CategoryData categoryData) {
		buf.writeInt(categoryData.getPointsLeft(experience));
		if (experience.isEnabled()) {
			buf.writeOptional(Optional.of(experience.getProgress(categoryData)), PacketByteBuf::writeFloat);
		} else {
			buf.writeOptional(Optional.empty(), PacketByteBuf::writeFloat);
		}
	}

	private void write(SkillsConfig skills, CategoryConfig category, CategoryData categoryData) {
		buf.writeCollection(skills.getAll(), (buf2, skill) -> write(skill, category, categoryData));
	}

	private void write(SkillConnectionsConfig connections) {
		buf.writeCollection(connections.getAll(), (buf2, connection) -> write(connection));
	}

	private void write(SkillConfig skill, CategoryConfig category, CategoryData categoryData) {
		buf.writeString(skill.getId());
		buf.writeInt(skill.getX());
		buf.writeInt(skill.getY());
		buf.writeString(skill.getDefinitionId());
		buf.writeBoolean(skill.isRoot());
		buf.writeEnumConstant(skill.getStateFor(category, categoryData));
	}

	private void write(SkillConnectionConfig skill) {
		buf.writeString(skill.getSkillAId());
		buf.writeString(skill.getSkillBId());
	}

	private void write(IconConfig icon) {
		buf.writeString(icon.getType());
		buf.writeNullable(icon.getData(), (buf1, element) -> buf1.writeString(element.toString()));
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.SHOW_CATEGORY;
	}
}
