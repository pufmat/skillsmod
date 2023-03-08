package net.puffish.skillsmod.client.data;

import net.puffish.skillsmod.skill.SkillState;

public class ClientSkillData {
	private final String id;
	private final int x;
	private final int y;
	private final String definitionId;
	private final boolean isRoot;
	private SkillState state;

	public ClientSkillData(String id, int x, int y, String definitionId, boolean isRoot, SkillState state) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.definitionId = definitionId;
		this.isRoot = isRoot;
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getDefinitionId() {
		return definitionId;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public SkillState getState() {
		return state;
	}

	public void setState(SkillState state) {
		this.state = state;
	}
}
