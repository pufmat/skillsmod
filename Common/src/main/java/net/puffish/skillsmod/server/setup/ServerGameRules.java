package net.puffish.skillsmod.server.setup;

import net.minecraft.world.GameRules;

public interface ServerGameRules {
	<T extends GameRules.Rule<T>> GameRules.Key<T> registerGameRule(String namespace, String name, GameRules.Category category, GameRules.Type<T> type);
}
