package net.puffish.skillsmod.api;

import net.minecraft.server.level.ServerPlayer;

public interface Experience {
	int getTotal(ServerPlayer player);

	void setTotal(ServerPlayer player, int amount);

	void addTotal(ServerPlayer player, int amount);

	/// Returns the current level based on the total experience.
	int getLevel(ServerPlayer player);

	/// Returns the current experience based on the total experience.
	int getCurrent(ServerPlayer player);

	/// Returns the experience required at the specified level.
	int getRequired(int level);

	@Deprecated
	int getRequired(ServerPlayer player, int level);

	/// Returns the total experience required at the specified level.
	int getRequiredTotal(int level);

	@Deprecated
	int getRequiredTotal(ServerPlayer player, int level);
}
