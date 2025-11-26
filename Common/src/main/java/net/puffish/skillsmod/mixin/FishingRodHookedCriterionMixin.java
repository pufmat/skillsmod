package net.puffish.skillsmod.mixin;

import net.minecraft.advancement.criterion.FishingRodHookedCriterion;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.FishItemExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(FishingRodHookedCriterion.class)
public class FishingRodHookedCriterionMixin {

	@Inject(method = "trigger", at = @At("HEAD"))
	private void injectAtInit(ServerPlayerEntity player, ItemStack rod, FishingBobberEntity bobber, Collection<ItemStack> fishingLoots, CallbackInfo ci) {
		for (var fishedItem : fishingLoots) {
			SkillsAPI.updateExperienceSources(
					player,
					FishItemExperienceSource.class,
					es -> (int) Math.round(es.calculation().evaluate(
							new FishItemExperienceSource.Data(player, rod, fishedItem)
					))
			);
		}
	}

}
