package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.network.InPacket;

public class ExperienceUpdateInPacket implements InPacket {
	private final String categoryId;
	private final float experienceProgress;

	private ExperienceUpdateInPacket(String categoryId, float experienceProgress) {
		this.categoryId = categoryId;
		this.experienceProgress = experienceProgress;
	}

	public static ExperienceUpdateInPacket read(PacketByteBuf buf) {
		var categoryId = buf.readString();
		var experienceProgress = buf.readFloat();

		return new ExperienceUpdateInPacket(
				categoryId,
				experienceProgress
		);
	}

	public String getCategoryId() {
		return categoryId;
	}

	public float getExperienceProgress() {
		return experienceProgress;
	}
}
