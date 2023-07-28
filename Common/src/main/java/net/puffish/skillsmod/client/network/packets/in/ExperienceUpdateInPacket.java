package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.InPacket;

public class ExperienceUpdateInPacket implements InPacket {
	private final Identifier categoryId;
	private final float experienceProgress;

	private ExperienceUpdateInPacket(Identifier categoryId, float experienceProgress) {
		this.categoryId = categoryId;
		this.experienceProgress = experienceProgress;
	}

	public static ExperienceUpdateInPacket read(PacketByteBuf buf) {
		var categoryId = buf.readIdentifier();
		var experienceProgress = buf.readFloat();

		return new ExperienceUpdateInPacket(
				categoryId,
				experienceProgress
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}

	public float getExperienceProgress() {
		return experienceProgress;
	}
}
