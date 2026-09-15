package net.puffish.skillsmod.access;

import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerEntity;

import java.util.Map;

public interface LivingEntityAccess {
	Map<ServerPlayerEntity, Float> getDamageShare();

	AntiFarmingPerEntity.State getAntiFarmingPerEntityState();
}
