package net.puffish.skillsmod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.BreakBlockExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "mineBlock", at = @At("HEAD"))
	private void injectAtPostMine(Level world, BlockState state, BlockPos pos, Player player, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			SkillsAPI.updateExperienceSources(
					serverPlayer,
					BreakBlockExperienceSource.class,
					experienceSource -> (int) Math.round(experienceSource.calculation().evaluate(
							new BreakBlockExperienceSource.Data(serverPlayer, state, (ItemStack) (Object) this)
					))
			);
		}
	}
}
