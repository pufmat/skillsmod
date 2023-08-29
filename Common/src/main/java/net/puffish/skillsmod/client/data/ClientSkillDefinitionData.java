package net.puffish.skillsmod.client.data;

import net.minecraft.text.Text;

public class ClientSkillDefinitionData {
	protected final String id;
	protected final Text title;
	protected final Text description;
	protected final ClientFrameData frame;
	protected final ClientIconData icon;

	public ClientSkillDefinitionData(String id, Text title, Text description, ClientFrameData frame, ClientIconData icon) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.frame = frame;
		this.icon = icon;
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

	public ClientFrameData getFrame() {
		return frame;
	}

	public ClientIconData getIcon() {
		return icon;
	}
}
