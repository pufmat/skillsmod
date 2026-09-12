package net.puffish.skillsmod.mixin;

import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.CriterionExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(SimpleCriterionTrigger.class)
public class SimpleCriterionTriggerMixin {

	@Inject(method = "trigger", at = @At("HEAD"))
	private void injectAtTrigger(ServerPlayer player, Predicate<Object> predicate, CallbackInfo ci) {
		SkillsAPI.updateExperienceSources(
				player,
				CriterionExperienceSource.class,
				es -> {
					if (es.criterion().trigger().equals(this)) {
						var conditions = es.criterion().triggerInstance();
						// That cast is valid since conditions in `SimpleCriterionTrigger` are instances of `SimpleCriterionTrigger.SimpleInstance`.
						if (predicate.test(conditions) && ((SimpleCriterionTrigger.SimpleInstance) conditions).player().map(lootContextPredicate -> {
							var lootContext = EntityPredicate.createContext(player, player);
							return lootContextPredicate.matches(lootContext);
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
