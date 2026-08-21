package net.puffish.skillsmod.impl;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Experience;
import net.puffish.skillsmod.api.Exchange;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.util.PointSources;

import java.util.Optional;
import java.util.stream.Stream;

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
	public Optional<Experience> getExperience() {
		if (SkillsMod.getInstance().hasExperience(categoryId).orElseThrow()) {
			return Optional.of(new ExperienceImpl(categoryId));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Exchange> getExchange() {
		if (SkillsMod.getInstance().hasExchange(categoryId).orElseThrow()) {
			return Optional.of(new ExchangeImpl(categoryId));
		} else {
			return Optional.empty();
		}
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
	public Stream<Skill> streamSkills() {
		return SkillsMod.getInstance()
				.getSkills(categoryId)
				.orElseThrow()
				.stream()
				.map(skillId -> new SkillImpl(this, skillId));
	}

	@Override
	public Stream<Skill> streamUnlockedSkills(ServerPlayer player) {
		return SkillsMod.getInstance()
				.getUnlockedSkills(player, categoryId)
				.orElseThrow()
				.stream()
				.map(skillId -> new SkillImpl(this, skillId));
	}

	@Override
	public void openScreen(ServerPlayer player) {
		SkillsMod.getInstance().openScreen(player, Optional.of(categoryId));
	}

	@Override
	public void unlock(ServerPlayer player) {
		SkillsMod.getInstance().unlockCategory(player, categoryId);
	}

	@Override
	public void lock(ServerPlayer player) {
		SkillsMod.getInstance().lockCategory(player, categoryId);
	}

	@Override
	public boolean isUnlocked(ServerPlayer player) {
		return SkillsMod.getInstance().isCategoryUnlocked(player, categoryId).orElseThrow();
	}

	@Override
	public void erase(ServerPlayer player) {
		SkillsMod.getInstance().eraseCategory(player, categoryId);
	}

	@Override
	public void resetSkills(ServerPlayer player) {
		SkillsMod.getInstance().resetSkills(player, categoryId);
	}

	@Override
	public Stream<Identifier> streamPointsSources(ServerPlayer player) {
		return SkillsMod.getInstance().getPointsSources(player, categoryId).orElseThrow();
	}

	@Override
	public int getPoints(ServerPlayer player, Identifier source) {
		return SkillsMod.getInstance().getPoints(player, categoryId, source).orElseThrow();
	}

	@Override
	public void setPoints(ServerPlayer player, Identifier source, int count) {
		SkillsMod.getInstance().setPoints(player, categoryId, source, count, false);
	}

	@Override
	public void addPoints(ServerPlayer player, Identifier source, int count) {
		SkillsMod.getInstance().addPoints(player, categoryId, source, count, false);
	}

	@Override
	public void setPointsSilently(ServerPlayer player, Identifier source, int count) {
		SkillsMod.getInstance().setPoints(player, categoryId, source, count, true);
	}

	@Override
	public void addPointsSilently(ServerPlayer player, Identifier source, int count) {
		SkillsMod.getInstance().addPoints(player, categoryId, source, count, true);
	}

	@Override
	public int getSpentPoints(ServerPlayer player) {
		return SkillsMod.getInstance().getSpentPoints(player, categoryId).orElseThrow();
	}

	@Override
	public int getPointsTotal(ServerPlayer player) {
		return SkillsMod.getInstance().getPointsTotal(player, categoryId).orElseThrow();
	}

	@Override
	public int getPointsLeft(ServerPlayer player) {
		return SkillsMod.getInstance().getPointsLeft(player, categoryId).orElseThrow();
	}

	@Override
	public int getExtraPoints(ServerPlayer player) {
		return getPointsTotal(player);
	}

	@Override
	public void setExtraPoints(ServerPlayer player, int count) {
		addExtraPoints(player, count - getExtraPoints(player));
	}

	@Override
	public void addExtraPoints(ServerPlayer player, int count) {
		addPoints(player, PointSources.COMMANDS, count);
	}
}
