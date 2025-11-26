package net.puffish.skillsmod.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.puffish.skillsmod.access.DamageSourceAccess;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.TakeDamageExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	@Inject(
			method = "applyDamage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/player/PlayerEntity;setHealth(F)V"
			)
	)
	private void injectAtSetHealth(ServerWorld world, DamageSource source, float damage, CallbackInfo ci) {
		if (((PlayerEntity) (Object) this) instanceof ServerPlayerEntity player) {
			var weapon = ((DamageSourceAccess) source).getWeapon().orElse(ItemStack.EMPTY);
			var takenDamage = Math.min(damage, player.getHealth());

			SkillsAPI.updateExperienceSources(
					player,
					TakeDamageExperienceSource.class,
					experienceSource -> (int) Math.round(experienceSource.calculation()
							.evaluate(new TakeDamageExperienceSource.Data(player, weapon, takenDamage, source)
					))
			);
		}
	}

}
