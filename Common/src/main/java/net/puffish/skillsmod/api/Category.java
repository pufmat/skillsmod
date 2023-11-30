package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Category {
	Identifier getId();

	Optional<Skill> getSkill(String skillId);

	List<Skill> getSkills();

	Collection<Skill> getUnlockedSkills(ServerPlayerEntity player);

	void unlock(ServerPlayerEntity player);

	void lock(ServerPlayerEntity player);

	void erase(ServerPlayerEntity player);

	void resetSkills(ServerPlayerEntity player);

	int getExperience(ServerPlayerEntity player);

	void setExperience(ServerPlayerEntity player, int amount);

	void addExperience(ServerPlayerEntity player, int amount);

	int getExtraPoints(ServerPlayerEntity player);

	void setExtraPoints(ServerPlayerEntity player, int count);

	void addExtraPoints(ServerPlayerEntity player, int count);

	int getPointsLeft(ServerPlayerEntity player);

	int getCurrentLevel(ServerPlayerEntity player);

	int getCurrentExperience(ServerPlayerEntity player);

	int getRequiredExperience(ServerPlayerEntity player);
}
