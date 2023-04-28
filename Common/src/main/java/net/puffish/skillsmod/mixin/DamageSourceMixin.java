package net.puffish.skillsmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
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
	private void injectAtInit(RegistryEntry<DamageType> type, Entity source, Entity attacker, Vec3d position, CallbackInfo ci) {
		if (attacker instanceof LivingEntity livingEntity) {
			weapon = livingEntity.getMainHandStack(); // not really correct
		}
	}

	@Override
	@Unique
	public Optional<ItemStack> getWeapon() {
		return Optional.ofNullable(weapon);
	}
}
