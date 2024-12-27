package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.experience.source.ExperienceSource;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceFactory;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardFactory;
import net.puffish.skillsmod.experience.source.ExperienceSourceRegistry;
import net.puffish.skillsmod.impl.CategoryImpl;
import net.puffish.skillsmod.reward.RewardRegistry;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class SkillsAPI {
	private SkillsAPI() { }
	
	public static final String MOD_ID = "puffish_skills";

	public static void registerReward(Identifier key, RewardFactory factory) {
		RewardRegistry.register(key, factory);
	}

	public static void registerExperienceSource(Identifier key, ExperienceSourceFactory factory) {
		ExperienceSourceRegistry.register(key, factory);
	}

	public static void updateExperienceSources(ServerPlayerEntity player, Function<ExperienceSource, Integer> function) {
		SkillsMod.getInstance().visitExperienceSources(player, function);
	}

	public static <T extends ExperienceSource> void updateExperienceSources(ServerPlayerEntity player, Class<T> clazz, Function<T, Integer> function) {
		SkillsMod.getInstance().visitExperienceSources(player, experienceSource -> {
			if (clazz.isInstance(experienceSource)) {
				return function.apply(clazz.cast(experienceSource));
			}
			return 0;
		});
	}

	public static void updateRewards(ServerPlayerEntity player, Identifier id) {
		SkillsMod.getInstance().updateRewards(player, reward -> reward.getType().equals(id));
	}

	public static void updateRewards(ServerPlayerEntity player, Predicate<Reward> predicate) {
		SkillsMod.getInstance().updateRewards(player, reward -> predicate.test(reward.getInstance()));
	}

	public static <T extends Reward> void updateRewards(ServerPlayerEntity player, Class<T> clazz) {
		updateRewards(player, clazz::isInstance);
	}

	public static <T extends Reward> void updateRewards(ServerPlayerEntity player, Class<T> clazz, Predicate<T> predicate) {
		updateRewards(player, reward -> {
			if (clazz.isInstance(reward)) {
				return predicate.test(clazz.cast(reward));
			}
			return false;
		});
	}

	public static void openScreen(ServerPlayerEntity player) {
		SkillsMod.getInstance().openScreen(player, Optional.empty());
	}

	public static Optional<Category> getCategory(Identifier categoryId) {
		if (SkillsMod.getInstance().hasCategory(categoryId)) {
			return Optional.of(new CategoryImpl(categoryId));
		} else {
			return Optional.empty();
		}
	}

	public static Stream<Category> streamCategories() {
		return SkillsMod.getInstance()
				.getCategories(false)
				.stream()
				.map(CategoryImpl::new);
	}

	public static Stream<Category> streamUnlockedCategories(ServerPlayerEntity player) {
		return SkillsMod.getInstance()
				.getUnlockedCategories(player)
				.stream()
				.map(CategoryImpl::new);
	}
}
