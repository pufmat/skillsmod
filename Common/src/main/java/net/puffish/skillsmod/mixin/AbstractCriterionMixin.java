package net.puffish.skillsmod.mixin;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
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
					if (es.criterion().trigger().equals(this)) {
						var conditions = es.criterion().conditions();
						// That cast is valid since conditions in `AbstractCriterion` are instances of `AbstractCriterion.Conditions`.
						if (predicate.test(conditions) && ((AbstractCriterion.Conditions) conditions).player().map(lootContextPredicate -> {
							var lootContext = EntityPredicate.createAdvancementEntityLootContext(player, player);
							return lootContextPredicate.test(lootContext);
						}).orElse(true)) {
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
