package net.puffish.skillsmod.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.access.DamageSourceAccess;
import net.puffish.skillsmod.access.LevelChunkAccess;
import net.puffish.skillsmod.access.LivingEntityAccess;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.DealDamageExperienceSource;
import net.puffish.skillsmod.util.AttackerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CombatTracker.class)
public class CombatTrackerMixin {

	@Shadow
	@Final
	private LivingEntity mob;

	@Inject(method = "recordDamage", at = @At("HEAD"))
	private void injectAtRecordDamage(DamageSource source, float damage, CallbackInfo ci) {
		AttackerInfo.detect(source.getEntity(), attackerInfo -> {
			var entityAccess = (LivingEntityAccess) mob;
			var damageShare = entityAccess.getDamageShare();
			var antiFarmingPerEntityState = entityAccess.getAntiFarmingPerEntityState();

			var weapon = ((DamageSourceAccess) source).getWeapon().orElse(ItemStack.EMPTY);
			var player = attackerInfo.player();

			var antiFarmingPerChunkState = ((LevelChunkAccess) mob.level()
					.getChunkAt(mob.blockPosition()))
					.getAntiFarmingPerChunkState();
			antiFarmingPerChunkState.removeOutdated();

			damageShare.compute(player, (key, value) -> {
				if (value == null) {
					return damage;
				} else {
					return value + damage;
				}
			});

			antiFarmingPerEntityState.removeOutdated();
			SkillsAPI.updateExperienceSources(
					player,
					DealDamageExperienceSource.class,
					es -> {
						if (attackerInfo.matchesTamedActivity(es.tamedActivity())
								&& es.antiFarmingPerChunk()
								.map(antiFarmingPerChunkState::tryIncrement)
								.orElse(true)
						) {
							float limitedDamage = es.antiFarmingPerEntity()
									.map(antiFarming -> antiFarmingPerEntityState.addAndLimit(
											antiFarming,
											damage
									))
									.orElse(damage);
							if (limitedDamage > Mth.EPSILON) {
								return (int) Math.round(es.calculation().evaluate(
										new DealDamageExperienceSource.Data(
												player,
												mob,
												weapon,
												limitedDamage,
												source
										)
								));
							}
						}
						return 0;
					}
			);
		});
	}

}
