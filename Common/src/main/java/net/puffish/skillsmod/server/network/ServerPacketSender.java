package net.puffish.skillsmod.server.network;

import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.network.OutPacket;

public interface ServerPacketSender {
	void send(ServerPlayerEntity player, OutPacket packet);
}
