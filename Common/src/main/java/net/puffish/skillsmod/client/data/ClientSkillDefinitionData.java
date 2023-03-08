package net.puffish.skillsmod.client.data;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.text.Text;

public class ClientSkillDefinitionData {
	protected final String id;
	protected final Text title;
	protected final Text description;
	protected final AdvancementFrame frame;
	protected final ClientIconData icon;

	public ClientSkillDefinitionData(String id, Text title, Text description, AdvancementFrame frame, ClientIconData icon) {
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

	public AdvancementFrame getFrame() {
		return frame;
	}

	public ClientIconData getIcon() {
		return icon;
	}
}
