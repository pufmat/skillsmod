package net.puffish.skillsmod.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Experience;

public class ExperienceImpl implements Experience {
	private final Identifier categoryId;

	public ExperienceImpl(Identifier categoryId) {
		this.categoryId = categoryId;
	}

	@Override
	public int getTotal(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getExperience(player, categoryId).orElseThrow();
	}

	@Override
	public void setTotal(ServerPlayerEntity player, int amount) {
		SkillsMod.getInstance().setExperience(player, categoryId, amount);
	}

	@Override
	public void addTotal(ServerPlayerEntity player, int amount) {
		SkillsMod.getInstance().addExperience(player, categoryId, amount);
	}

	@Override
	public int getLevel(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getCurrentLevel(player, categoryId).orElseThrow();
	}

	@Override
	public int getCurrent(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getCurrentExperience(player, categoryId).orElseThrow();
	}

	@Override
	public int getRequired(ServerPlayerEntity player, int level) {
		return SkillsMod.getInstance().getRequiredExperience(player, categoryId, level).orElseThrow();
	}

	@Override
	public int getRequiredTotal(ServerPlayerEntity player, int level) {
		return SkillsMod.getInstance().getRequiredTotalExperience(player, categoryId, level).orElseThrow();
	}
}
