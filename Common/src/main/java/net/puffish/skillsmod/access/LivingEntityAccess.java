package net.puffish.skillsmod.access;

import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerEntity;

import java.util.Map;

public interface LivingEntityAccess {
	Map<ServerPlayer, Float> getDamageShare();

	AntiFarmingPerEntity.State getAntiFarmingPerEntityState();
}
