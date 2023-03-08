package net.puffish.skillsmod.client.data;

public class ClientSkillConnectionData {
	private final String skillAId;
	private final String skillBId;

	public ClientSkillConnectionData(String skillAId, String skillBId) {
		this.skillAId = skillAId;
		this.skillBId = skillBId;
	}

	public String getSkillAId() {
		return skillAId;
	}

	public String getSkillBId() {
		return skillBId;
	}
}
