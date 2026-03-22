package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.puffish.skillsmod.network.InPacket;
import net.puffish.skillsmod.util.ToastType;

public class ShowToastInPacket implements InPacket {

	private final ToastType type;

	private ShowToastInPacket(ToastType type) {
		this.type = type;
	}

	public static ShowToastInPacket read(FriendlyByteBuf buf) {
		return new ShowToastInPacket(buf.readEnum(ToastType.class));
	}

	public ToastType getToastType() {
		return type;
	}
}
