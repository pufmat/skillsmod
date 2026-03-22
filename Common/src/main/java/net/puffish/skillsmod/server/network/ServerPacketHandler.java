package net.puffish.skillsmod.server.network;

import net.minecraft.server.level.ServerPlayer;

public interface ServerPacketHandler<T> {
	void handle(ServerPlayer player, T packet);
}
