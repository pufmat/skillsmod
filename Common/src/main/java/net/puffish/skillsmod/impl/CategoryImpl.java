package net.puffish.skillsmod.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CategoryImpl implements Category {
	private final Identifier categoryId;

	public CategoryImpl(Identifier categoryId) {
		this.categoryId = categoryId;
	}

	@Override
	public Identifier getId() {
		return categoryId;
	}

	@Override
	public Optional<Skill> getSkill(String skillId) {
		if (SkillsMod.getInstance().hasSkill(categoryId, skillId)) {
			return Optional.of(new SkillImpl(this, skillId));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public List<Skill> getSkills() {
		return SkillsMod.getInstance()
				.getSkills(categoryId)
				.orElseThrow()
				.stream()
				.map(skillId -> (Skill) new SkillImpl(this, skillId))
				.toList();
	}

	@Override
	public Collection<Skill> getUnlockedSkills(ServerPlayerEntity player) {
		return SkillsMod.getInstance()
				.getUnlockedSkills(player, categoryId)
				.orElseThrow()
				.stream()
				.map(skillId -> (Skill) new SkillImpl(this, skillId))
				.toList();
	}

	@Override
	public void unlock(ServerPlayerEntity player) {
		SkillsMod.getInstance().unlockCategory(player, categoryId);
	}

	@Override
	public void lock(ServerPlayerEntity player) {
		SkillsMod.getInstance().lockCategory(player, categoryId);
	}

	@Override
	public void erase(ServerPlayerEntity player) {
		SkillsMod.getInstance().eraseCategory(player, categoryId);
	}

	@Override
	public void resetSkills(ServerPlayerEntity player) {
		SkillsMod.getInstance().resetSkills(player, categoryId);
	}

	@Override
	public int getExperience(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getExperience(player, categoryId).orElseThrow();
	}

	@Override
	public void setExperience(ServerPlayerEntity player, int amount) {
		SkillsMod.getInstance().setExperience(player, categoryId, amount);
	}

	@Override
	public void addExperience(ServerPlayerEntity player, int amount) {
		SkillsMod.getInstance().addExperience(player, categoryId, amount);
	}

	@Override
	public int getExtraPoints(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getExtraPoints(player, categoryId).orElseThrow();
	}

	@Override
	public void setExtraPoints(ServerPlayerEntity player, int count) {
		SkillsMod.getInstance().setExtraPoints(player, categoryId, count);
	}

	@Override
	public void addExtraPoints(ServerPlayerEntity player, int count) {
		SkillsMod.getInstance().addExtraPoints(player, categoryId, count);
	}

	@Override
	public int getPointsLeft(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getPointsLeft(player, categoryId).orElseThrow();
	}

	@Override
	public int getCurrentLevel(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getCurrentLevel(player, categoryId).orElseThrow();
	}

	@Override
	public int getCurrentExperience(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getCurrentExperience(player, categoryId).orElseThrow();
	}

	@Override
	public int getRequiredExperience(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getRequiredExperience(player, categoryId).orElseThrow();
	}
}
