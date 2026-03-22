package net.puffish.skillsmod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.MineBlockExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {

	@Inject(method = "playerDestroy", at = @At("HEAD"))
	private void injectAtAfterBreak(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			SkillsAPI.updateExperienceSources(
					serverPlayer,
					MineBlockExperienceSource.class,
					es -> (int) Math.round(es.calculation().evaluate(
							new MineBlockExperienceSource.Data(serverPlayer, state, stack)
					))
			);
		}
	}
}
