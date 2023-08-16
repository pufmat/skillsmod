package net.puffish.skillsmod.server.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;

import java.util.HashSet;
import java.util.Set;

public class CategoryData {
	private final Set<String> unlockedSkills;

	private boolean unlocked;
	private int extraPoints;
	private int earnedExperience;

	private CategoryData(Set<String> unlockedSkills, boolean unlocked, int extraPoints, int earnedExperience) {
		this.unlockedSkills = unlockedSkills;
		this.unlocked = unlocked;
		this.extraPoints = extraPoints;
		this.earnedExperience = earnedExperience;
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
		nbt.putInt("points", extraPoints);
		nbt.putInt("experience", earnedExperience);

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
		this.earnedExperience += experience;
	}

	public Set<String> getUnlockedSkillIds() {
		return unlockedSkills;
	}

	public int getEarnedExperience() {
		return earnedExperience;
	}

	public void setEarnedExperience(int earnedExperience) {
		this.earnedExperience = earnedExperience;
	}

	public int getCurrentLevel(CategoryConfig category) {
		return category.getExperience().getCurrentLevel(earnedExperience);
	}

	public int getCurrentExperience(CategoryConfig category) {
		return category.getExperience().getCurrentExperience(earnedExperience);
	}

	public int getRequiredExperience(CategoryConfig category) {
		return category.getExperience().getRequiredExperience(getCurrentLevel(category));
	}

	public int getPointsForExperience(CategoryConfig category) {
		if (category.getExperience().isEnabled()) {
			return getCurrentLevel(category);
		}
		return 0;
	}

	public int getSpentPoints(CategoryConfig category) {
		return unlockedSkills.stream()
				.flatMap(skillId -> category.getSkills()
						.getById(skillId)
						.flatMap(skill -> category.getDefinitions().getById(skill.getDefinitionId()))
						.stream()
				)
				.mapToInt(SkillDefinitionConfig::getCost)
				.sum();
	}

	public int getEarnedPoints(CategoryConfig category) {
		return getExtraPoints() + getPointsForExperience(category);
	}

	public int getPointsLeft(CategoryConfig category) {
		return getEarnedPoints(category) - getSpentPoints(category);
	}

	public void setPointsLeft(int count, CategoryConfig category) {
		addExtraPoints(count - getPointsLeft(category));
	}

	public void addExtraPoints(int count) {
		extraPoints += count;
	}

	public int getExtraPoints() {
		return extraPoints;
	}

	public void setExtraPoints(int points) {
		this.extraPoints = points;
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	public void setUnlocked(boolean unlocked) {
		this.unlocked = unlocked;
	}
}
