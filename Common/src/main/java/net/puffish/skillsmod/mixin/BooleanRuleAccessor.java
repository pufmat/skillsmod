package net.puffish.skillsmod.mixin;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.BooleanRule.class)
public interface BooleanRuleAccessor {
	@Invoker("create")
	static GameRules.Type<GameRules.BooleanRule> puffish_skills$create(boolean initialValue) {
		throw new AssertionError();
	}
}
