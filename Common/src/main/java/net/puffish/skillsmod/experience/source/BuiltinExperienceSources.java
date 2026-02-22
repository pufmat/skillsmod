package net.puffish.skillsmod.experience.source;

import net.puffish.skillsmod.experience.source.builtin.BreakBlockExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.CraftItemExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.CriterionExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.DealDamageExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.EatFoodExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.EnchantItemExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.FishItemExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.HealExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.IncreaseStatExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.KillEntityExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.MineBlockExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.SharedKillEntityExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.SmeltItemExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.TakeDamageExperienceSource;

public class BuiltinExperienceSources {
	public static void register() {
		BreakBlockExperienceSource.register();
		CraftItemExperienceSource.register();
		CriterionExperienceSource.register();
		DealDamageExperienceSource.register();
		EatFoodExperienceSource.register();
		EnchantItemExperienceSource.register();
		FishItemExperienceSource.register();
		HealExperienceSource.register();
		IncreaseStatExperienceSource.register();
		KillEntityExperienceSource.register();
		MineBlockExperienceSource.register();
		SharedKillEntityExperienceSource.register();
		SmeltItemExperienceSource.register();
		TakeDamageExperienceSource.register();
	}
}
