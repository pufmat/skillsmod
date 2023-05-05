package net.puffish.skillsmod.mixin;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.puffish.skillsmod.attributes.PlayerAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	@Inject(method = "createPlayerAttributes", at = @At("RETURN"), cancellable = true)
	private static void injectAtCreatePlayerAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
				.add(PlayerAttributes.STAMINA)
				.add(PlayerAttributes.FORTUNE)
				.add(PlayerAttributes.RANGED_DAMAGE)
				.add(PlayerAttributes.MELEE_DAMAGE)
				.add(PlayerAttributes.HEALING)
				.add(PlayerAttributes.JUMP)
				.add(PlayerAttributes.RESISTANCE)
				.add(PlayerAttributes.MINING_SPEED)
		);
	}
}
