package net.puffish.skillsmod.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.skill.SkillState;

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
	public boolean isUnlocked(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getSkillState(player, category.getId(), skillId).orElseThrow() == SkillState.UNLOCKED;
	}

	@Override
	public void unlock(ServerPlayerEntity player) {
		SkillsMod.getInstance().unlockSkill(player, category.getId(), skillId);
	}
}
