package net.puffish.skillsmod.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.IncreaseStatExperienceSource;
import net.puffish.skillsmod.reward.builtin.AttributeReward;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void injectAtInit(CallbackInfo ci) {
		SkillsAPI.updateRewards((ServerPlayer) (Object) this, AttributeReward.class);
	}

	@Inject(method = "awardStat", at = @At("HEAD"))
	private void injectAtAwardStat(Stat<?> stat, int amount, CallbackInfo ci) {
		var player = (ServerPlayer) (Object) this;
		SkillsAPI.updateExperienceSources(
				player,
				IncreaseStatExperienceSource.class,
				es -> (int) Math.round(es.calculation().evaluate(
						new IncreaseStatExperienceSource.Data(player, stat, amount)
				))
		);
	}
}
