package net.puffish.skillsmod.server.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.puffish.skillsmod.config.experience.ExperienceConfig;

import java.util.HashSet;
import java.util.Set;

public class CategoryData {
	private final Set<String> unlockedSkills;

	private boolean unlocked;
	private int points;
	private int experience;

	private CategoryData(Set<String> unlockedSkills, boolean unlocked, int points, int experience) {
		this.unlockedSkills = unlockedSkills;
		this.unlocked = unlocked;
		this.points = points;
		this.experience = experience;
	}

	public static CategoryData create(boolean unlocked) {
		return new CategoryData(new HashSet<>(), unlocked, 0, 0);
	}

	public static CategoryData read(NbtCompound nbt) {
		var unlocked = nbt.getBoolean("unlocked");
		var points = nbt.getInt("points");
		var experience = nbt.getInt("experience");

		var unlockedSkills = new HashSet<String>();
		var unlockedNbt = nbt.getList("unlocked_skills", NbtElement.STRING_TYPE);
		for (var elementNbt : unlockedNbt) {
			if (elementNbt instanceof NbtString stringNbt) {
				unlockedSkills.add(stringNbt.asString());
			}
		}

		return new CategoryData(unlockedSkills, unlocked, points, experience);
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putBoolean("unlocked", unlocked);
		nbt.putInt("points", points);
		nbt.putInt("experience", experience);

		NbtList unlockedNbt = new NbtList();
		for (var skill : unlockedSkills) {
			unlockedNbt.add(NbtString.of(skill));
		}
		nbt.put("unlocked_skills", unlockedNbt);

		return nbt;
	}

	public void unlockSkill(String id) {
		unlockedSkills.add(id);
	}

	public void resetSkills() {
		unlockedSkills.clear();
	}

	public void addExperience(int experience) {
		this.experience += experience;
	}

	public Set<String> getUnlockedSkillIds() {
		return unlockedSkills;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public int getPointsForExperience(ExperienceConfig experience) {
		if (!experience.isEnabled()) {
			return 0;
		}
		return experience.getLevel(this);
	}

	public int getSpentPoints() {
		return getUnlockedSkillIds().size();
	}

	public int getPointsLeft(ExperienceConfig experience) {
		return getExtraPoints() + getPointsForExperience(experience) - getSpentPoints();
	}

	public void setPointsLeft(int count, ExperienceConfig experience) {
		addExtraPoints(count - getPointsLeft(experience));
	}

	public void addExtraPoints(int count) {
		points += count;
	}

	public int getExtraPoints() {
		return points;
	}

	public void setExtraPoints(int points) {
		this.points = points;
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	public void setUnlocked(boolean unlocked) {
		this.unlocked = unlocked;
	}
}
