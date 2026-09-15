package net.puffish.skillsmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.access.DamageSourceAccess;
import net.puffish.skillsmod.access.LivingEntityAccess;
import net.puffish.skillsmod.access.WorldChunkAccess;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.DealDamageExperienceSource;
import net.puffish.skillsmod.util.AttackerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin {

	@Shadow
	@Final
	private LivingEntity entity;

	@Inject(method = "onDamage", at = @At("HEAD"))
	private void injectAtOnDamage(DamageSource source, float originalHealth, float damage, CallbackInfo ci) {
		AttackerInfo.detect(source.getAttacker(), attackerInfo -> {
			var entityAccess = (LivingEntityAccess) entity;
			var damageShare = entityAccess.getDamageShare();
			var antiFarmingPerEntityState = entityAccess.getAntiFarmingPerEntityState();

			var weapon = ((DamageSourceAccess) source).getWeapon().orElse(ItemStack.EMPTY);
			var player = attackerInfo.player();

			var antiFarmingPerChunkState = ((WorldChunkAccess) entity.getWorld()
					.getWorldChunk(entity.getBlockPos()))
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
							if (limitedDamage > MathHelper.EPSILON) {
								return (int) Math.round(es.calculation().evaluate(
										new DealDamageExperienceSource.Data(
												player,
												entity,
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
