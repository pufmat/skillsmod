package net.puffish.skillsmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.puffish.skillsmod.access.DamageSourceAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin implements DamageSourceAccess {

	@Shadow
	public abstract Entity getAttacker();

	@Override
	@Unique
	public Optional<ItemStack> getWeapon() {
		if (getAttacker() instanceof LivingEntity livingEntity) {
			return Optional.ofNullable(livingEntity.getMainHandStack()); // not really correct
		}
		return Optional.empty();
	}
}
