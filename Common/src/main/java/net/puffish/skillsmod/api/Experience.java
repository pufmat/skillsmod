package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;

public interface Experience {
	int getTotal(ServerPlayerEntity player);

	void setTotal(ServerPlayerEntity player, int amount);

	void addTotal(ServerPlayerEntity player, int amount);

	/// Returns the current level based on the total experience.
	int getLevel(ServerPlayerEntity player);

	/// Returns the current experience based on the total experience.
	int getCurrent(ServerPlayerEntity player);

	/// Returns the experience required at the specified level.
	int getRequired(int level);

	@Deprecated
	int getRequired(ServerPlayerEntity player, int level);

	/// Returns the total experience required at the specified level.
	int getRequiredTotal(int level);

	@Deprecated
	int getRequiredTotal(ServerPlayerEntity player, int level);
}
