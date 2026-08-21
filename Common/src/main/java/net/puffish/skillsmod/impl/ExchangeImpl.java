package net.puffish.skillsmod.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Exchange;

public class ExchangeImpl implements Exchange {
	private final Identifier categoryId;

	public ExchangeImpl(Identifier categoryId) {
		this.categoryId = categoryId;
	}

	@Override
	public int getLevel(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getExchangeLevel(player, categoryId).orElseThrow();
	}

	@Override
	public void setLevel(ServerPlayerEntity player, int level) {
		SkillsMod.getInstance().setExchangeLevel(player, categoryId, level);
	}

	@Override
	public int getCost(int level) {
		return SkillsMod.getInstance().getCost(categoryId, level).orElseThrow();
	}

}
