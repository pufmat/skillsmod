package net.puffish.skillsmod.mixin;

import net.minecraft.advancement.criterion.EnchantedItemCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.EnchantItemExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantedItemCriterion.class)
public class EnchantedItemCriterionMixin {

	@Inject(method = "trigger", at = @At("HEAD"))
	private void injectAtTrigger(ServerPlayerEntity serverPlayer, ItemStack stack, int levels, CallbackInfo ci) {
		SkillsAPI.updateExperienceSources(
				serverPlayer,
				EnchantItemExperienceSource.class,
				es -> (int) Math.round(es.calculation().evaluate(
						new EnchantItemExperienceSource.Data(serverPlayer, stack, levels)
				))
		);
	}
}
