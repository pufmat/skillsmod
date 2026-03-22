package net.puffish.skillsmod.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.puffish.skillsmod.experience.source.builtin.util.TamedActivity;

import java.util.function.Consumer;

public record AttackerInfo(
		ServerPlayer player,
		boolean isTamed
) {

	public static void detect(Entity entity, Consumer<AttackerInfo> consumer) {
		if (entity instanceof ServerPlayer player) {
			consumer.accept(new AttackerInfo(player, false));
		} else if (entity instanceof OwnableEntity tameable) {
			if (tameable.getOwner() instanceof ServerPlayer player) {
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
