package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Category {
	private final Identifier categoryId;

	public Category(Identifier categoryId) {
		this.categoryId = categoryId;
	}

	public Identifier getId() {
		return categoryId;
	}

	public Optional<Skill> getSkill(String skillId) {
		if (SkillsMod.getInstance().hasSkill(categoryId, skillId)) {
			return Optional.of(new Skill(this, skillId));
		} else {
			return Optional.empty();
		}
	}

	private List<Skill> getSkills() {
		return SkillsMod.getInstance()
				.getSkills(categoryId)
				.orElseThrow()
				.stream()
				.map(skillId -> new Skill(this, skillId))
				.toList();
	}

	public Collection<Skill> getUnlockedSkills(ServerPlayerEntity player) {
		return SkillsMod.getInstance()
				.getUnlockedSkills(player, categoryId)
				.orElseThrow()
				.stream()
				.map(skillId -> new Skill(this, skillId))
				.toList();
	}

	public void unlock(ServerPlayerEntity player) {
		SkillsMod.getInstance().unlockCategory(player, categoryId);
	}

	public void lock(ServerPlayerEntity player) {
		SkillsMod.getInstance().lockCategory(player, categoryId);
	}

	public void erase(ServerPlayerEntity player) {
		SkillsMod.getInstance().eraseCategory(player, categoryId);
	}

	public void resetSkills(ServerPlayerEntity player) {
		SkillsMod.getInstance().resetSkills(player, categoryId);
	}

	public int getExperience(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getExperience(player, categoryId).orElseThrow();
	}

	public void setExperience(ServerPlayerEntity player, int amount) {
		SkillsMod.getInstance().setExperience(player, categoryId, amount);
	}

	public void addExperience(ServerPlayerEntity player, int amount) {
		SkillsMod.getInstance().addExperience(player, categoryId, amount);
	}

	public int getExtraPoints(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getExtraPoints(player, categoryId).orElseThrow();
	}

	public void setExtraPoints(ServerPlayerEntity player, int count) {
		SkillsMod.getInstance().setExtraPoints(player, categoryId, count);
	}

	public void addExtraPoints(ServerPlayerEntity player, int count) {
		SkillsMod.getInstance().addExtraPoints(player, categoryId, count);
	}

	public int getPointsLeft(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getPointsLeft(player, categoryId).orElseThrow();
	}

	public int getCurrentLevel(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getCurrentLevel(player, categoryId).orElseThrow();
	}

	public int getCurrentExperience(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getCurrentExperience(player, categoryId).orElseThrow();
	}

	public int getRequiredExperience(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getRequiredExperience(player, categoryId).orElseThrow();
	}
}
