package net.puffish.skillsmod.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Tameable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.experience.source.builtin.util.TamedActivity;

import java.util.function.Consumer;

public record AttackerInfo(
		ServerPlayerEntity player,
		boolean isTamed
) {

	public static void detect(Entity entity, Consumer<AttackerInfo> consumer) {
		if (entity instanceof ServerPlayerEntity player) {
			consumer.accept(new AttackerInfo(player, false));
		} else if (entity instanceof Tameable tameable) {
			if (tameable.getOwner() instanceof ServerPlayerEntity player) {
				consumer.accept(new AttackerInfo(player, true));
			}
		}
	}

	public boolean matchesTamedActivity(TamedActivity tamedActivity) {
		return switch (tamedActivity) {
			case EXCLUDE -> !isTamed;
			case INCLUDE -> true;
			case ONLY -> isTamed;
		};
	}

}
