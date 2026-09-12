package net.puffish.skillsmod.mixin;

import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.puffish.skillsmod.access.AbstractCriterionConditionsAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractCriterionConditions.class)
public class AbstractCriterionConditionsMixin implements AbstractCriterionConditionsAccess {
	@Shadow
	@Final
	private LootContextPredicate playerPredicate;

	@Override
	public LootContextPredicate getPlayerPredicate() {
		return playerPredicate;
	}
}
