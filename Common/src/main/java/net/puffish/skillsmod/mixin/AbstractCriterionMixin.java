package net.puffish.skillsmod.mixin;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.access.AbstractCriterionConditionsAccess;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.CriterionExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(AbstractCriterion.class)
public class AbstractCriterionMixin {

	@Inject(method = "trigger", at = @At("HEAD"))
	private void injectAtTrigger(ServerPlayerEntity player, Predicate<Object> predicate, CallbackInfo ci) {
		SkillsAPI.updateExperienceSources(
				player,
				CriterionExperienceSource.class,
				es -> {
					var conditions = es.criterion().getConditions();
					if (conditions != null && this.equals(Criteria.getById(conditions.getId()))) {
						var lootContext = EntityPredicate.createAdvancementEntityLootContext(player, player);
						// That cast is valid since conditions in `AbstractCriterion` are instances of `AbstractCriterionConditions`.
						if (predicate.test(conditions) && ((AbstractCriterionConditionsAccess) conditions).getPlayerPredicate().test(lootContext)) {
							return (int) Math.round(es.calculation().evaluate(
									new CriterionExperienceSource.Data(player)
							));
						}
					}
					return 0;
				}
		);
	}

}
