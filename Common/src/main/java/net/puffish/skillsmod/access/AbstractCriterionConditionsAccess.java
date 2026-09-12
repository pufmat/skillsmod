package net.puffish.skillsmod.access;

import net.minecraft.predicate.entity.LootContextPredicate;

public interface AbstractCriterionConditionsAccess {
	LootContextPredicate getPlayerPredicate();
}
