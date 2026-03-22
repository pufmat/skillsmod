package net.puffish.skillsmod.server.network;

import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.network.OutPacket;

public interface ServerPacketSender {
	void send(ServerPlayer player, OutPacket packet);
}
