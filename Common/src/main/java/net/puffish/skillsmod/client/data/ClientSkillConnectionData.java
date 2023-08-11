package net.puffish.skillsmod.client.data;

public class ClientSkillConnectionData {
	private final String skillAId;
	private final String skillBId;
	private final boolean bidirectional;

	public ClientSkillConnectionData(String skillAId, String skillBId, boolean bidirectional) {
		this.skillAId = skillAId;
		this.skillBId = skillBId;
		this.bidirectional = bidirectional;
	}

	public String getSkillAId() {
		return skillAId;
	}

	public String getSkillBId() {
		return skillBId;
	}

	public boolean isBidirectional() {
		return bidirectional;
	}
}
