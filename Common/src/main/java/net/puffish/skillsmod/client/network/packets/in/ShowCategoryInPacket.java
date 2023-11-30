package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.client.data.ClientFrameData;
import net.puffish.skillsmod.client.data.ClientIconData;
import net.puffish.skillsmod.client.data.ClientSkillCategoryData;
import net.puffish.skillsmod.client.data.ClientSkillConnectionData;
import net.puffish.skillsmod.client.data.ClientSkillData;
import net.puffish.skillsmod.client.data.ClientSkillDefinitionData;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.network.InPacket;
import net.puffish.skillsmod.skill.SkillState;

import java.util.Optional;
import java.util.stream.Collectors;

public class ShowCategoryInPacket implements InPacket {
	private final ClientSkillCategoryData category;

	private ShowCategoryInPacket(ClientSkillCategoryData category) {
		this.category = category;
	}

	public static ShowCategoryInPacket read(PacketByteBuf buf) {
		var category = readCategory(buf);

		return new ShowCategoryInPacket(category);
	}

	public static ClientSkillCategoryData readCategory(PacketByteBuf buf) {
		var id = buf.readIdentifier();

		var title = buf.readText();
		var icon = readSkillIcon(buf);
		var background = buf.readIdentifier();
		var exclusiveRoot = buf.readBoolean();
		var spentPointsLimit = buf.readInt();

		var definitions = buf.readList(ShowCategoryInPacket::readDefinition)
				.stream()
				.collect(Collectors.toMap(ClientSkillDefinitionData::getId, definition -> definition));

		var skills = buf.readList(ShowCategoryInPacket::readSkill)
				.stream()
				.collect(Collectors.toMap(ClientSkillData::getId, skill -> skill));

		var normalConnections = buf.readList(ShowCategoryInPacket::readSkillConnection);
		var exclusiveConnections = buf.readList(ShowCategoryInPacket::readSkillConnection);

		var spentPoints = buf.readInt();
		var earnedPoints = buf.readInt();

		var currentLevel = Integer.MIN_VALUE;
		var currentExperience = Integer.MIN_VALUE;
		var requiredExperience = Integer.MIN_VALUE;
		if (buf.readBoolean()) {
			currentLevel = buf.readInt();
			currentExperience = buf.readInt();
			requiredExperience = buf.readInt();
		}

		return new ClientSkillCategoryData(
				id,
				title,
				icon,
				background,
				exclusiveRoot,
				spentPointsLimit,
				definitions,
				skills,
				normalConnections,
				exclusiveConnections,
				spentPoints,
				earnedPoints,
				currentLevel,
				currentExperience,
				requiredExperience
		);
	}

	public static ClientSkillDefinitionData readDefinition(PacketByteBuf buf) {
		var id = buf.readString();
		var title = buf.readText();
		var description = buf.readText();
		var extraDescription = buf.readText();
		var frame = readFrameIcon(buf);
		var icon = readSkillIcon(buf);
		var size = buf.readFloat();

		return new ClientSkillDefinitionData(id, title, description, extraDescription, frame, icon, size);
	}

	public static ClientIconData readSkillIcon(PacketByteBuf buf) {
		var type = buf.readString();
		return buf.readOptional(PacketByteBuf::readString)
				.flatMap(data -> JsonElementWrapper.parseString(data, JsonPath.named("Client Skill Icon")).getSuccess())
				.flatMap(rootElement -> switch (type) {
					case "item" -> ClientIconData.ItemIconData.parse(rootElement).getSuccess();
					case "effect" -> ClientIconData.EffectIconData.parse(rootElement).getSuccess();
					case "texture" -> ClientIconData.TextureIconData.parse(rootElement).getSuccess();
					default -> Optional.empty();
				}).orElseGet(ClientIconData.TextureIconData::createMissing);
	}

	public static ClientFrameData readFrameIcon(PacketByteBuf buf) {
		var type = buf.readString();
		return buf.readOptional(PacketByteBuf::readString)
				.flatMap(data -> JsonElementWrapper.parseString(data, JsonPath.named("Client Frame Icon")).getSuccess())
				.flatMap(rootElement -> switch (type) {
					case "advancement" -> ClientFrameData.AdvancementFrameData.parse(rootElement).getSuccess();
					case "texture" -> ClientFrameData.TextureFrameData.parse(rootElement).getSuccess();
					default -> Optional.empty();
				}).orElseGet(ClientFrameData.TextureFrameData::createMissing);
	}

	public static ClientSkillData readSkill(PacketByteBuf buf) {
		var id = buf.readString();
		var x = buf.readInt();
		var y = buf.readInt();
		var definition = buf.readString();
		var isRoot = buf.readBoolean();
		var state = buf.readEnumConstant(SkillState.class);

		return new ClientSkillData(id, x, y, definition, isRoot, state);
	}

	public static ClientSkillConnectionData readSkillConnection(PacketByteBuf buf) {
		var skillAId = buf.readString();
		var skillBId = buf.readString();
		var bidirectional = buf.readBoolean();

		return new ClientSkillConnectionData(skillAId, skillBId, bidirectional);
	}

	public ClientSkillCategoryData getCategory() {
		return category;
	}
}
