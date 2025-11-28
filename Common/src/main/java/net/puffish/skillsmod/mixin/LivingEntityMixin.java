package net.puffish.skillsmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.server.world.ServerWorld;
import net.puffish.skillsmod.access.DamageSourceAccess;
import net.puffish.skillsmod.access.WorldChunkAccess;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.DealDamageExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.HealExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.KillEntityExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.SharedKillEntityExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerEntity;
import net.puffish.skillsmod.util.AttackerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Unique
	private int entityDroppedXp = 0;

	@Unique
	private final Map<ServerPlayerEntity, Float> damageShare = new WeakHashMap<>();

	@Unique
	private final AntiFarmingPerEntity.State antiFarmingPerEntityState = new AntiFarmingPerEntity.State();

	@Inject(method = "heal", at = @At("TAIL"))
	private void injectAtHeal(float amount, CallbackInfo ci) {
		if (amount > 0) {
			if (((LivingEntity) (Object) this) instanceof ServerPlayerEntity player) {
				SkillsAPI.updateExperienceSources(
						player,
						HealExperienceSource.class,
						es -> (int) Math.round(es.calculation().evaluate(
								new HealExperienceSource.Data(player, amount)
						))
				);
			}
		}
	}

	@Inject(method = "applyDamage", at = @At("TAIL"))
	private void injectAtApplyDamage(ServerWorld world, DamageSource source, float damage, CallbackInfo ci) {
		AttackerInfo.detect(source.getAttacker(), attackerInfo -> {
			var entity = ((LivingEntity) (Object) this);
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

	@Inject(method = "drop", at = @At("TAIL"))
	private void injectAtDrop(ServerWorld world, DamageSource source, CallbackInfo ci) {
		AttackerInfo.detect(source.getAttacker(), attackerInfo -> {
			var entity = ((LivingEntity) (Object) this);
			var weapon = ((DamageSourceAccess) source).getWeapon().orElse(ItemStack.EMPTY);
			var player = attackerInfo.player();

			var antiFarmingPerChunkState = ((WorldChunkAccess) entity.getWorld()
					.getWorldChunk(entity.getBlockPos()))
					.getAntiFarmingPerChunkState();
			antiFarmingPerChunkState.removeOutdated();

			SkillsAPI.updateExperienceSources(
					player,
					KillEntityExperienceSource.class,
					es -> {
						if (attackerInfo.matchesTamedActivity(es.tamedActivity())
								&& es
								.antiFarmingPerChunk()
								.map(antiFarmingPerChunkState::tryIncrement)
								.orElse(true)
						) {
							return (int) Math.round(es.calculation().evaluate(
									new KillEntityExperienceSource.Data(
											player,
											entity,
											weapon,
											source,
											entityDroppedXp
									)
							));
						}
						return 0;
					}
			);

			var entries = damageShare.entrySet();
			var totalDamage = entries.stream().mapToDouble(Map.Entry::getValue).sum();
			for (var entry : entries) {
				SkillsAPI.updateExperienceSources(
						entry.getKey(),
						SharedKillEntityExperienceSource.class,
						es -> {
							if (attackerInfo.matchesTamedActivity(es.tamedActivity())
									&& es.antiFarmingPerChunk()
									.map(antiFarmingPerChunkState::tryIncrement)
									.orElse(true)
							) {
								return (int) Math.round(es.calculation().evaluate(
										new SharedKillEntityExperienceSource.Data(
												entry.getKey(),
												entity,
												weapon,
												source,
												entityDroppedXp,
												totalDamage,
												entries.size(),
												entry.getValue() / totalDamage
										)
								));
							}
							return 0;
						}
				);
			}
		});
	}

	@ModifyArg(
			method = "dropExperience",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"
			),
			index = 2
	)
	private int injectAtDropExperience(int droppedXp) {
		entityDroppedXp = droppedXp;
		return droppedXp;
	}
}
