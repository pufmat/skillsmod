package net.puffish.skillsmod.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.puffish.skillsmod.access.EntityAttributeInstanceAccess;
import net.puffish.skillsmod.attributes.PlayerAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

	@Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
	private void injectAtGetBlockBreakingSpeed(CallbackInfoReturnable<Float> cir) {
		var player = ((PlayerInventory) (Object) this).player;
		var attribute = (EntityAttributeInstanceAccess) player.getAttributeInstance(PlayerAttributes.MINING_SPEED);
		cir.setReturnValue((float) attribute.computeValueForInitial(cir.getReturnValueF()));
	}

}
