package net.puffish.skillsmod.mixin;

import net.minecraft.advancement.criterion.ConsumeItemCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.builtin.EatFoodExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConsumeItemCriterion.class)
public class ConsumeItemCriterionMixin {

	@Inject(method = "trigger", at = @At("HEAD"))
	private void injectAtTrigger(ServerPlayerEntity serverPlayer, ItemStack stack, CallbackInfo ci) {
		var food = stack.getItem().getFoodComponent();
		if (food != null) {
			SkillsAPI.visitExperienceSources(serverPlayer, experienceSource -> {
				if (experienceSource instanceof EatFoodExperienceSource eatFoodExperienceSource) {
					return eatFoodExperienceSource.getValue(serverPlayer, stack);
				}
				return 0;
			});
		}
	}
}
