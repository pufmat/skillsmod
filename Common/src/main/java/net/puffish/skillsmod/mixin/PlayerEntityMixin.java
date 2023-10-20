package net.puffish.skillsmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.access.EntityAttributeInstanceAccess;
import net.puffish.skillsmod.server.PlayerAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	private static final double VANILLA_KNOCKBACK = 0.4;

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
				.add(PlayerAttributes.SPRINTING_SPEED)
				.add(PlayerAttributes.KNOCKBACK)
		);
	}

	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;onTargetDamaged(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/Entity;)V"))
	private void injectAtAttack(Entity target, CallbackInfo ci) {
		var player = (PlayerEntity) (Object) this;

		var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(PlayerAttributes.KNOCKBACK));
		var knockback = attribute.computeIncreasedValueForInitial(VANILLA_KNOCKBACK) - VANILLA_KNOCKBACK;

		var yaw = player.getYaw() * MathHelper.RADIANS_PER_DEGREE;
		var sin = MathHelper.sin(yaw);
		var cos = MathHelper.cos(yaw);

		if (target instanceof LivingEntity livingEntity) {
			livingEntity.takeKnockback(knockback, sin, -cos);
		} else {
			target.addVelocity(-sin * knockback, 0, cos * knockback);
		}
	}

	@Inject(method = "getMovementSpeed()F", at = @At("RETURN"), cancellable = true)
	private void injectAtGetMovementSpeed(CallbackInfoReturnable<Float> cir) {
		var player = (PlayerEntity) (Object) this;

		if (player.isSprinting()) {
			var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(PlayerAttributes.SPRINTING_SPEED));
			cir.setReturnValue((float) attribute.computeIncreasedValueForInitial(cir.getReturnValueF()));
		}
	}

}
