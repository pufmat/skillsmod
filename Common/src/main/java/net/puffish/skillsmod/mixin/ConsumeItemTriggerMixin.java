package net.puffish.skillsmod.mixin;

import net.minecraft.advancements.criterion.ConsumeItemTrigger;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.EatFoodExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConsumeItemTrigger.class)
public class ConsumeItemTriggerMixin {

	@Inject(method = "trigger", at = @At("HEAD"))
	private void injectAtTrigger(ServerPlayer serverPlayer, ItemStack stack, CallbackInfo ci) {
		var fc = stack.getItem().components().get(DataComponents.FOOD);
		if (fc != null) {
			SkillsAPI.updateExperienceSources(
					serverPlayer,
					EatFoodExperienceSource.class,
					es -> (int) Math.round(es.calculation().evaluate(
							new EatFoodExperienceSource.Data(serverPlayer, stack)
					))
			);
		}
	}
}
