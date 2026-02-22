package net.puffish.skillsmod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.CraftItemExperienceSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin {

	@Shadow
	@Final
	private PlayerEntity player;

	@Shadow
	private int amount;

	@Inject(
			method = "onCrafted(Lnet/minecraft/item/ItemStack;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;onCraftByPlayer(Lnet/minecraft/entity/player/PlayerEntity;I)V"
			)
	)
	private void injectAtOnCraftByPlayer(ItemStack stack, CallbackInfo ci) {
		if (player instanceof ServerPlayerEntity serverPlayer) {
			SkillsAPI.updateExperienceSources(
					serverPlayer,
					CraftItemExperienceSource.class,
					es -> (int) Math.round(es.calculation().evaluate(
							new CraftItemExperienceSource.Data(serverPlayer, stack)
					) * amount)
			);
		}
	}

}
