package net.puffish.skillsmod.client.network;

public interface ClientPacketHandler<T> {
	void handle(T packet);
}
