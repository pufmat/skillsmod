package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Category {
	Identifier getId();

	Optional<Experience> getExperience();

	Optional<Skill> getSkill(String skillId);

	List<Skill> getSkills();

	Collection<Skill> getUnlockedSkills(ServerPlayerEntity player);

	void resetSkills(ServerPlayerEntity player);

	void unlock(ServerPlayerEntity player);

	void lock(ServerPlayerEntity player);

	void erase(ServerPlayerEntity player);

	int getExtraPoints(ServerPlayerEntity player);

	void setExtraPoints(ServerPlayerEntity player, int count);

	void addExtraPoints(ServerPlayerEntity player, int count);

	int getPointsLeft(ServerPlayerEntity player);
}
