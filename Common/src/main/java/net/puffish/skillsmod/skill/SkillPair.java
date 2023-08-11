package net.puffish.skillsmod.skill;

public record SkillPair(String skillAId, String skillBId) {
	public enum Direction {
		A_TO_B,
		B_TO_A,
		BOTH
	}
}
