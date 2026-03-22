package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.util.ToastType;

public record ShowToastOutPacket(ToastType type) implements OutPacket {
	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeEnum(type);
	}

	@Override
	public Identifier getId() {
		return Packets.SHOW_TOAST;
	}
}
