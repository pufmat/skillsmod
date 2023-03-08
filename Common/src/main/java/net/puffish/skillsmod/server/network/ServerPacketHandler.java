package net.puffish.skillsmod.server.network;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ServerPacketHandler<T> {
	void handle(ServerPlayerEntity player, T packet);
}
