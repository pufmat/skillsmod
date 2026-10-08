package net.puffish.skillsmod.mixin;

import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.LootContextPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractCriterionConditions.class)
public interface AbstractCriterionConditionsAccessor {
	@Accessor("playerPredicate")
	LootContextPredicate getPlayerPredicate();
}
