package net.puffish.skillsmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.access.DamageSourceAccess;
import net.puffish.skillsmod.access.EntityAttributeInstanceAccess;
import net.puffish.skillsmod.access.WorldChunkAccess;
import net.puffish.skillsmod.experience.builtin.KillEntityExperienceSource;
import net.puffish.skillsmod.experience.builtin.TakeDamageExperienceSource;
import net.puffish.skillsmod.server.setup.SkillsAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Unique
	private int entityDroppedXp = 0;

	@ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float modifyVariableAtDamage(float damage, DamageSource source) {
		if (damage < 0) {
			return damage;
		}

		if (source.getAttacker() instanceof PlayerEntity player) {
			if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
				var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(SkillsAttributes.RANGED_DAMAGE));
				damage = (float) attribute.computeIncreasedValueForInitial(damage);
			} else {
				var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(SkillsAttributes.MELEE_DAMAGE));
				damage = (float) attribute.computeIncreasedValueForInitial(damage);
			}
		}
		return damage;
	}

	@Inject(method = "damage", at = @At("TAIL"))
	private void injectAtDamage(DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
		if (((LivingEntity) (Object) this) instanceof ServerPlayerEntity serverPlayer) {
			SkillsAPI.visitExperienceSources(serverPlayer, experienceSource -> {
				if (experienceSource instanceof TakeDamageExperienceSource takeDamageExperienceSource) {
					return takeDamageExperienceSource.getValue(serverPlayer, damage, source);
				}
				return 0;
			});
		}
	}

	@ModifyVariable(method = "heal", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float modifyVariableAtHeal(float amount) {
		if (amount < 0) {
			return amount;
		}

		if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
			var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(SkillsAttributes.HEALING));
			amount = (float) attribute.computeIncreasedValueForInitial(amount);
		}
		return amount;
	}

	@Inject(method = "getJumpVelocity", at = @At("RETURN"), cancellable = true)
	private void injectAtGetJumpVelocity(CallbackInfoReturnable<Float> cir) {
		if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
			var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(SkillsAttributes.JUMP));
			cir.setReturnValue((float) attribute.computeIncreasedValueForInitial(cir.getReturnValueF()));
		}
	}

	@ModifyVariable(method = "computeFallDamage", at = @At("STORE"), ordinal = 2)
	private float modifyVariableAtComputeFallDamage(float reduction) {
		if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
			var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(SkillsAttributes.JUMP));
			reduction += (attribute.computeIncreasedValueForInitial(1.0f) - 1.0f) * 10.0f;
		}
		return reduction;
	}

	@Inject(method = "drop", at = @At("TAIL"))
	private void injectAtDrop(DamageSource source, CallbackInfo ci) {
		if (source.getAttacker() instanceof ServerPlayerEntity player) {
			var entity = ((LivingEntity) (Object) this);
			var weapon = ((DamageSourceAccess) source).getWeapon().orElse(ItemStack.EMPTY);

			WorldChunkAccess worldChunk = ((WorldChunkAccess) entity.getWorld().getWorldChunk(entity.getBlockPos()));
			worldChunk.antiFarmingCleanupOutdated();
			SkillsAPI.visitExperienceSources(player, experienceSource -> {
				if (experienceSource instanceof KillEntityExperienceSource entityExperienceSource) {
					if (entityExperienceSource
							.getAntiFarming()
							.map(worldChunk::antiFarmingAddAndCheck)
							.orElse(true)
					) {
						return entityExperienceSource.getValue(player, entity, weapon, source, entityDroppedXp);
					}
				}
				return 0;
			});
		}

	}

	@ModifyArg(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"), index = 2)
	private int injectAtDropXp(int droppedXp) {
		entityDroppedXp = droppedXp;
		return droppedXp;
	}

	@Inject(method = "modifyAppliedDamage", at = @At("TAIL"), cancellable = true)
	private void injectAtModifyAppliedDamage(CallbackInfoReturnable<Float> cir) {
		if (((LivingEntity) (Object) this) instanceof PlayerEntity player && cir.getReturnValueF() < Float.MAX_VALUE / 3.0f) {
			var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(SkillsAttributes.RESISTANCE));
			cir.setReturnValue(Math.max(
					0.0f,
					(float) attribute.computeDecreasedValueForInitial(cir.getReturnValueF())
			));
		}
	}
}
