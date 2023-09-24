package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.experience.ExperienceSource;
import net.puffish.skillsmod.experience.ExperienceSourceRegistry;
import net.puffish.skillsmod.api.experience.ExperienceSourceWithDataFactory;
import net.puffish.skillsmod.api.experience.ExperienceSourceWithoutDataFactory;
import net.puffish.skillsmod.rewards.RewardRegistry;
import net.puffish.skillsmod.api.rewards.RewardWithDataFactory;
import net.puffish.skillsmod.api.rewards.RewardWithoutDataFactory;

import java.util.List;
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

	public static Optional<Category> getCategory(Identifier categoryId) {
		if (SkillsMod.getInstance().hasCategory(categoryId)) {
			return Optional.of(new Category(categoryId));
		} else {
			return Optional.empty();
		}
	}

	public static List<Category> getCategories() {
		return SkillsMod.getInstance().getCategories().stream().map(Category::new).toList();
	}

	public static List<Category> getUnlockedCategories(ServerPlayerEntity player) {
		return SkillsMod.getInstance()
				.getUnlockedCategories(player)
				.stream()
				.map(Category::new)
				.toList();
	}
}
