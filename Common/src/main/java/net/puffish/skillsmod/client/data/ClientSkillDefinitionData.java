package net.puffish.skillsmod.client.data;

import net.minecraft.text.Text;

public class ClientSkillDefinitionData {
	private final String id;
	private final Text title;
	private final Text description;
	private final Text extraDescription;
	private final ClientFrameData frame;
	private final ClientIconData icon;
	private final float size;

	public ClientSkillDefinitionData(String id, Text title, Text description, Text extraDescription, ClientFrameData frame, ClientIconData icon, float size) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.extraDescription = extraDescription;
		this.frame = frame;
		this.icon = icon;
		this.size = size;
	}

	public String getId() {
		return id;
	}

	public Text getTitle() {
		return title;
	}

	public Text getDescription() {
		return description;
	}

	public Text getExtraDescription() {
		return extraDescription;
	}

	public ClientFrameData getFrame() {
		return frame;
	}

	public ClientIconData getIcon() {
		return icon;
	}

	public float getSize() {
		return size;
	}
}
