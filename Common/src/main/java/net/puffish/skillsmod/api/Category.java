package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.stream.Stream;

public interface Category {
	Identifier getId();

	Optional<Experience> getExperience();

	Optional<Skill> getSkill(String skillId);

	Stream<Skill> streamSkills();

	Stream<Skill> streamUnlockedSkills(ServerPlayerEntity player);

	void openScreen(ServerPlayerEntity player);

	void resetSkills(ServerPlayerEntity player);

	void unlock(ServerPlayerEntity player);

	void lock(ServerPlayerEntity player);

	boolean isUnlocked(ServerPlayerEntity player);

	void erase(ServerPlayerEntity player);

	Stream<Identifier> streamPointsSources(ServerPlayerEntity player);

	int getPoints(ServerPlayerEntity player, Identifier source);

	void setPoints(ServerPlayerEntity player, Identifier source, int count);

	void addPoints(ServerPlayerEntity player, Identifier source, int count);

	void setPointsSilently(ServerPlayerEntity player, Identifier source, int count);

	void addPointsSilently(ServerPlayerEntity player, Identifier source, int count);

	int getSpentPoints(ServerPlayerEntity player);

	int getPointsTotal(ServerPlayerEntity player);

	int getPointsLeft(ServerPlayerEntity player);

	@Deprecated
	int getExtraPoints(ServerPlayerEntity player);

	@Deprecated
	void setExtraPoints(ServerPlayerEntity player, int count);

	@Deprecated
	void addExtraPoints(ServerPlayerEntity player, int count);
}
