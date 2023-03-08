package net.puffish.skillsmod.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.SkillsAPI;
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
}
