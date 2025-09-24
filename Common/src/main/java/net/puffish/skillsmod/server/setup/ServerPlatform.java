package net.puffish.skillsmod.server.setup;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ServerPlatform {
	boolean isFakePlayer(ServerPlayerEntity player);
	boolean isModLoaded(String id);
}
