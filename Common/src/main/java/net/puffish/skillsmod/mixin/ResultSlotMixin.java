package net.puffish.skillsmod.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.CraftItemExperienceSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {

	@Shadow
	@Final
	private Player player;

	@Shadow
	private int removeCount;

	@Inject(
			method = "checkTakeAchievements(Lnet/minecraft/world/item/ItemStack;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;onCraftedBy(Lnet/minecraft/world/entity/player/Player;I)V"
			)
	)
	private void injectAtOnCraftedBy(ItemStack stack, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			SkillsAPI.updateExperienceSources(
					serverPlayer,
					CraftItemExperienceSource.class,
					es -> (int) Math.round(es.calculation().evaluate(
							new CraftItemExperienceSource.Data(serverPlayer, stack)
					) * removeCount)
			);
		}
	}

}
