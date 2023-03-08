package net.puffish.skillsmod.client.network;

import net.puffish.skillsmod.network.OutPacket;

public interface ClientPacketSender {
	void send(OutPacket packet);
}
