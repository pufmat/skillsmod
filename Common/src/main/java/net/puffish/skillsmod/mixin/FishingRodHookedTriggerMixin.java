package net.puffish.skillsmod.mixin;

import net.minecraft.advancements.criterion.FishingRodHookedTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.FishItemExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(FishingRodHookedTrigger.class)
public class FishingRodHookedTriggerMixin {

	@Inject(method = "trigger", at = @At("HEAD"))
	private void injectAtTrigger(ServerPlayer player, ItemStack rod, FishingHook bobber, Collection<ItemStack> fishingLoots, CallbackInfo ci) {
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
