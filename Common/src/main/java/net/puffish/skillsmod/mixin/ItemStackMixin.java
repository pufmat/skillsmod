package net.puffish.skillsmod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.BreakBlockExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.CraftItemExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "onCraft", at = @At("HEAD"))
	private void injectAtOnCraft(World world, PlayerEntity player, int amount, CallbackInfo ci) {
		if (player instanceof ServerPlayerEntity serverPlayer) {
			SkillsAPI.updateExperienceSources(
					serverPlayer,
					CraftItemExperienceSource.class,
					experienceSource -> experienceSource.getValue(serverPlayer, (ItemStack) (Object) this) * amount
			);
		}
	}

	@Inject(method = "postMine", at = @At("HEAD"))
	private void injectAtPostMine(World world, BlockState state, BlockPos pos, PlayerEntity player, CallbackInfo ci) {
		if (player instanceof ServerPlayerEntity serverPlayer) {
			SkillsAPI.updateExperienceSources(
					serverPlayer,
					BreakBlockExperienceSource.class,
					experienceSource -> experienceSource.getValue(serverPlayer, state, (ItemStack) (Object) this)
			);
		}
	}
}
