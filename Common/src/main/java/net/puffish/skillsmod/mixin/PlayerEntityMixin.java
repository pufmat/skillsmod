package net.puffish.skillsmod.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.access.DamageSourceAccess;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.TakeDamageExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {

	@Inject(
			method = "actuallyHurt",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;setHealth(F)V"
			)
	)
	private void injectAtSetHealth(ServerLevel world, DamageSource source, float damage, CallbackInfo ci) {
		if (((Player) (Object) this) instanceof ServerPlayer player) {
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
