package net.puffish.skillsmod.mixin;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.puffish.skillsmod.server.setup.SkillsAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

	@ModifyConstant(method = "update", constant = @Constant(floatValue = 4.0f, ordinal = 0))
	private float modifyConstant0AtUpdate(float value, PlayerEntity player) {
		return getStamina(player);
	}

	@ModifyConstant(method = "update", constant = @Constant(floatValue = 4.0f, ordinal = 1))
	private float modifyConstant1AtUpdate(float value, PlayerEntity player) {
		return getStamina(player);
	}

	@Unique
	private float getStamina(PlayerEntity player) {
		return (float) player.getAttributeValue(SkillsAttributes.STAMINA);
	}
}
