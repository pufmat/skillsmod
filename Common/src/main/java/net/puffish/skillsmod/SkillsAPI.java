package net.puffish.skillsmod;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.experience.ExperienceSource;
import net.puffish.skillsmod.experience.ExperienceSourceWithDataFactory;
import net.puffish.skillsmod.experience.ExperienceSourceWithoutDataFactory;
import net.puffish.skillsmod.experience.ExperienceSourceRegistry;
import net.puffish.skillsmod.rewards.RewardRegistry;
import net.puffish.skillsmod.rewards.RewardWithDataFactory;
import net.puffish.skillsmod.rewards.RewardWithoutDataFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public class SkillsAPI {
	public static final String MOD_ID = "puffish_skills";

	public static void registerRewardWithData(Identifier key, RewardWithDataFactory factory) {
		RewardRegistry.register(key, factory);
	}

	public static void registerRewardWithoutData(Identifier key, RewardWithoutDataFactory factory) {
		RewardRegistry.register(key, factory);
	}

	public static void refreshReward(ServerPlayerEntity player, Identifier key) {
		SkillsMod.getInstance().refreshReward(player, key);
	}

	public static void registerExperienceSourceWithData(Identifier key, ExperienceSourceWithDataFactory factory) {
		ExperienceSourceRegistry.register(key, factory);
	}

	public static void registerExperienceSourceWithoutData(Identifier key, ExperienceSourceWithoutDataFactory factory) {
		ExperienceSourceRegistry.register(key, factory);
	}

	public static void visitExperienceSources(ServerPlayerEntity player, Function<ExperienceSource, Integer> function) {
		SkillsMod.getInstance().visitExperienceSources(player, function);
	}

	public static Optional<Collection<String>> getSkills(String categoryId) {
		return SkillsMod.getInstance().getSkills(categoryId);
	}

	public static Optional<Collection<String>> getUnlockedSkills(ServerPlayerEntity player, String categoryId) {
		return SkillsMod.getInstance().getUnlockedSkills(player, categoryId);
	}

	public static Collection<String> getCategories() {
		return SkillsMod.getInstance().getCategories();
	}

	public static Collection<String> getUnlockedCategories(ServerPlayerEntity player) {
		return SkillsMod.getInstance().getUnlockedCategories(player);
	}

	public static void unlockCategory(ServerPlayerEntity player, String categoryId) {
		SkillsMod.getInstance().unlockCategory(player, categoryId);
	}

	public static void lockCategory(ServerPlayerEntity player, String categoryId) {
		SkillsMod.getInstance().lockCategory(player, categoryId);
	}

	public static void eraseCategory(ServerPlayerEntity player, String categoryId) {
		SkillsMod.getInstance().eraseCategory(player, categoryId);
	}

	public static void unlockSkill(ServerPlayerEntity player, String categoryId, String skillId) {
		SkillsMod.getInstance().unlockSkill(player, categoryId, skillId);
	}

	public static void resetSkills(ServerPlayerEntity player, String categoryId) {
		SkillsMod.getInstance().resetSkills(player, categoryId);
	}

	public static Optional<Integer> getExperience(ServerPlayerEntity player, String categoryId) {
		return SkillsMod.getInstance().getExperience(player, categoryId);
	}

	public static void setExperience(ServerPlayerEntity player, String categoryId, int amount) {
		SkillsMod.getInstance().setExperience(player, categoryId, amount);
	}

	public static void addExperience(ServerPlayerEntity player, String categoryId, int amount) {
		SkillsMod.getInstance().addExperience(player, categoryId, amount);
	}

	public static Optional<Integer> getExtraPoints(ServerPlayerEntity player, String categoryId) {
		return SkillsMod.getInstance().getExtraPoints(player, categoryId);
	}

	public static void setExtraPoints(ServerPlayerEntity player, String categoryId, int count) {
		SkillsMod.getInstance().setExtraPoints(player, categoryId, count);
	}

	public static void addExtraPoints(ServerPlayerEntity player, String categoryId, int count) {
		SkillsMod.getInstance().addExtraPoints(player, categoryId, count);
	}

	public static Optional<Integer> getPointsLeft(ServerPlayerEntity player, String categoryId) {
		return SkillsMod.getInstance().getPointsLeft(player, categoryId);
	}

	public static void setPointsLeft(ServerPlayerEntity player, String categoryId, int count) {
		SkillsMod.getInstance().setPointsLeft(player, categoryId, count);
	}
}
