package net.puffish.skillsmod.mixin;

import net.minecraft.advancements.triggers.EnchantedItemTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.EnchantItemExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantedItemTrigger.class)
public class EnchantedItemTriggerMixin {

	@Inject(method = "trigger", at = @At("HEAD"))
	private void injectAtTrigger(ServerPlayer serverPlayer, ItemStack stack, int levels, CallbackInfo ci) {
		SkillsAPI.updateExperienceSources(
				serverPlayer,
				EnchantItemExperienceSource.class,
				es -> (int) Math.round(es.calculation().evaluate(
						new EnchantItemExperienceSource.Data(serverPlayer, stack, levels)
				))
		);
	}
}
