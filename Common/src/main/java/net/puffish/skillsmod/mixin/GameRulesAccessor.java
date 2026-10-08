package net.puffish.skillsmod.mixin;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.class)
public interface GameRulesAccessor {
	@Invoker("register")
	static <T extends GameRules.Rule<T>> GameRules.Key<T> puffish_skills$register(String name, GameRules.Category category, GameRules.Type<T> type) {
		throw new AssertionError();
	}
}
