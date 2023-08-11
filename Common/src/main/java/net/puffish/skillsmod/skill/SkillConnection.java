package net.puffish.skillsmod.skill;

public record SkillConnection(String skillAId, String skillBId, boolean bidirectional) {

	public static SkillConnection createBidirectional(String skillAId, String skillBId) {
		return new SkillConnection(skillAId, skillBId, true);
	}

	public static SkillConnection createUnidirectional(String skillAId, String skillBId) {
		return new SkillConnection(skillAId, skillBId, false);
	}

}
