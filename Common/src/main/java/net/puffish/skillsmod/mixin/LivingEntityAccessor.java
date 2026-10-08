package net.puffish.skillsmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Invoker("shouldDropXp")
	boolean puffish_skills$shouldDropXp();

	@Invoker("getXpToDrop")
	int puffish_skills$getXpToDrop(PlayerEntity player);
}
