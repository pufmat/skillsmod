package net.puffish.skillsmod.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.builtin.IncreaseStatExperienceSource;
import net.puffish.skillsmod.rewards.builtin.AttributeReward;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void injectAtInit(CallbackInfo ci) {
		SkillsAPI.refreshReward((ServerPlayerEntity) (Object) this, AttributeReward.ID);
	}

	@Inject(method = "increaseStat", at = @At("HEAD"))
	private void injectAtIncreaseStat(Stat<?> stat, int amount, CallbackInfo ci) {
		var player = (ServerPlayerEntity) (Object) this;
		SkillsAPI.visitExperienceSources(player, experienceSource -> {
			if (experienceSource instanceof IncreaseStatExperienceSource increaseStatExperienceSource) {
				return increaseStatExperienceSource.getValue(player, stat, amount);
			}
			return 0;
		});
	}
}
