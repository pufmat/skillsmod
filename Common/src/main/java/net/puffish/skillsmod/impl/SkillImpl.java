package net.puffish.skillsmod.impl;

import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;

public class SkillImpl implements Skill {
	private final Category category;
	private final String skillId;

	public SkillImpl(Category category, String skillId) {
		this.category = category;
		this.skillId = skillId;
	}

	@Override
	public Category getCategory() {
		return category;
	}

	@Override
	public String getId() {
		return skillId;
	}

	@Override
	public State getState(ServerPlayer player) {
		return SkillsMod.getInstance().getSkillState(player, category.getId(), skillId).orElseThrow();
	}

	@Override
	public void unlock(ServerPlayer player) {
		SkillsMod.getInstance().unlockSkill(player, category.getId(), skillId);
	}

	@Override
	public void lock(ServerPlayer player) {
		SkillsMod.getInstance().lockSkill(player, category.getId(), skillId);
	}
}
