package net.puffish.skillsmod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.builtin.CraftItemExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "onCraft", at = @At("HEAD"))
	private void injectAtOnCraft(World world, PlayerEntity player, int amount, CallbackInfo ci) {
		if (player instanceof ServerPlayerEntity serverPlayer) {
			SkillsAPI.visitExperienceSources(serverPlayer, experienceSource -> {
				if (experienceSource instanceof CraftItemExperienceSource craftItemExperienceSource) {
					return craftItemExperienceSource.getValue(serverPlayer, (ItemStack) (Object) this) * amount;
				}
				return 0;
			});
		}
	}
}
