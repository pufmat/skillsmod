package net.puffish.skillsmod.api;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public interface Category {
	Identifier getId();

	Optional<Experience> getExperience();

	Optional<Exchange> getExchange();

	Optional<Skill> getSkill(String skillId);

	Stream<Skill> streamSkills();

	Stream<Skill> streamUnlockedSkills(ServerPlayer player);

	void openScreen(ServerPlayer player);

	void resetSkills(ServerPlayer player);

	void unlock(ServerPlayer player);

	void lock(ServerPlayer player);

	boolean isUnlocked(ServerPlayer player);

	void erase(ServerPlayer player);

	Stream<Identifier> streamPointsSources(ServerPlayer player);

	int getPoints(ServerPlayer player, Identifier source);

	void setPoints(ServerPlayer player, Identifier source, int count);

	void addPoints(ServerPlayer player, Identifier source, int count);

	void setPointsSilently(ServerPlayer player, Identifier source, int count);

	void addPointsSilently(ServerPlayer player, Identifier source, int count);

	int getSpentPoints(ServerPlayer player);

	int getPointsTotal(ServerPlayer player);

	int getPointsLeft(ServerPlayer player);

	@Deprecated
	int getExtraPoints(ServerPlayer player);

	@Deprecated
	void setExtraPoints(ServerPlayer player, int count);

	@Deprecated
	void addExtraPoints(ServerPlayer player, int count);
}
