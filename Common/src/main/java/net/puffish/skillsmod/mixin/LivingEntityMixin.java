package net.puffish.skillsmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.access.EntityAttributeInstanceAccess;
import net.puffish.skillsmod.access.WorldChunkAccess;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.attributes.PlayerAttributes;
import net.puffish.skillsmod.experience.builtin.KillEntityExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float modifyVariableAtDamage(float damage, DamageSource source) {
		if (damage < 0) {
			return damage;
		}

		if (source.getAttacker() instanceof PlayerEntity player) {
			if (source.isOf(DamageTypes.MOB_PROJECTILE)) {
				var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(PlayerAttributes.RANGED_DAMAGE));
				damage = (float) attribute.computeValueForInitial(damage);
			} else {
				var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(PlayerAttributes.MELEE_DAMAGE));
				damage = (float) attribute.computeValueForInitial(damage);
			}
		}
		return damage;
	}

	@ModifyVariable(method = "heal", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float modifyVariableAtHeal(float amount) {
		if (amount < 0) {
			return amount;
		}

		if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
			var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(PlayerAttributes.HEALING));
			amount = (float) attribute.computeValueForInitial(amount);
		}
		return amount;
	}

	@Inject(method = "getJumpVelocity", at = @At("RETURN"), cancellable = true)
	private void injectAtGetJumpVelocity(CallbackInfoReturnable<Float> cir) {
		if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
			var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(PlayerAttributes.JUMP));
			cir.setReturnValue((float) attribute.computeValueForInitial(cir.getReturnValueF()));
		}
	}

	@ModifyVariable(method = "computeFallDamage", at = @At("STORE"), ordinal = 2)
	private float modifyVariableAtComputeFallDamage(float reduction) {
		if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
			var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(PlayerAttributes.JUMP));
			reduction += (attribute.computeValueForInitial(1.0f) - 1.0f) * 10.0f;
		}
		return reduction;
	}

	@Inject(method = "drop", at = @At("HEAD"))
	private void injectAtDrop(DamageSource source, CallbackInfo ci) {
		if (source.getAttacker() instanceof ServerPlayerEntity player) {
			var entity = ((LivingEntity) (Object) this);
			if (entity.shouldDropXp()) {
				WorldChunkAccess worldChunk = ((WorldChunkAccess) entity.getWorld().getWorldChunk(entity.getBlockPos()));
				worldChunk.antiFarmingCleanupOutdated();
				SkillsAPI.visitExperienceSources(player, experienceSource -> {
					if (experienceSource instanceof KillEntityExperienceSource entityExperienceSource) {
						if (worldChunk.antiFarmingAddAndCheck(entityExperienceSource.getAntiFarming())) {
							return entityExperienceSource.getValue(entity.getType(), entity.getXpToDrop());
						}
					}
					return 0;
				});
			}

		}
	}

	@Inject(method = "modifyAppliedDamage", at = @At("TAIL"), cancellable = true)
	private void injectAtModifyAppliedDamage(CallbackInfoReturnable<Float> cir) {
		if (((LivingEntity) (Object) this) instanceof PlayerEntity player && cir.getReturnValueF() < Float.MAX_VALUE / 3.0f) {
			var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(PlayerAttributes.RESISTANCE));
			cir.setReturnValue(Math.max(
					0.0f,
					2.0f * cir.getReturnValueF() - ((float) attribute.computeValueForInitial(cir.getReturnValueF()))
			));
		}
	}
}
