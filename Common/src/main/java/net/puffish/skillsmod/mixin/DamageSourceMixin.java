package net.puffish.skillsmod.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.puffish.skillsmod.access.DamageSourceAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceAccess {
	@Unique
	private ItemStack weapon;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void injectAtInit(Holder<DamageType> type, Entity source, Entity attacker, Vec3 position, CallbackInfo ci) {
		if (attacker instanceof LivingEntity livingEntity) {
			weapon = livingEntity.getMainHandItem(); // not really correct
		}
	}

	@Override
	@Unique
	public Optional<ItemStack> getWeapon() {
		return Optional.ofNullable(weapon);
	}
}
