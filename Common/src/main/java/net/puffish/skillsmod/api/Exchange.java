package net.puffish.skillsmod.api;

import net.minecraft.server.level.ServerPlayer;

public interface Exchange {

	/// Returns the current level.
	int getLevel(ServerPlayer player);

	/// Sets level.
	void setLevel(ServerPlayer player, int level);

	/// Adds level.
	void addLevel(ServerPlayer player, int level);

	/// Returns the cost at the specified level.
	int getCost(int level);

}
