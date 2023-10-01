package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.skill.SkillState;

public class Skill {
	private final Category category;
	private final String skillId;

	public Skill(Category category, String skillId) {
		this.category = category;
		this.skillId = skillId;
	}

	public Category getCategory() {
		return category;
	}

	public String getId() {
		return skillId;
	}

	public boolean isUnlocked(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getSkillState(player, category.getId(), skillId).orElseThrow() == SkillState.UNLOCKED;
	}

	public void unlock(ServerPlayerEntity player) {
		SkillsMod.getInstance().unlockSkill(player, category.getId(), skillId);
	}
}
