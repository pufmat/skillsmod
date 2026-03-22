package net.puffish.skillsmod.server.setup;

import net.minecraft.server.level.ServerPlayer;

public interface ServerPlatform {
	boolean isFakePlayer(ServerPlayer player);
	boolean isModLoaded(String id);
}
