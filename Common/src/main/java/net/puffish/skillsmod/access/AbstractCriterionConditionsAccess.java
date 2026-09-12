package net.puffish.skillsmod.access;

import net.minecraft.predicate.entity.EntityPredicate;

public interface AbstractCriterionConditionsAccess {
	EntityPredicate.Extended getPlayerPredicate();
}
