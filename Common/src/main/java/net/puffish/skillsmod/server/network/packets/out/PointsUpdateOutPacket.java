package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public record PointsUpdateOutPacket(Identifier categoryId, int spentPoints, int earnedPoints) implements OutPacket {
	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeIdentifier(categoryId);
		buf.writeInt(spentPoints);
		buf.writeInt(earnedPoints);
	}

	@Override
	public Identifier getId() {
		return Packets.POINTS_UPDATE;
	}
}
