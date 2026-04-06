package net.puffish.skillsmod.mixin;

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
						if (predicate.test(es.criterion().triggerInstance())) {
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
