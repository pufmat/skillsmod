package net.puffish.skillsmod.access;

import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerEntity;

import java.util.Map;

public interface LivingEntityAccess {
	Map<ServerPlayerEntity, Float> puffish_skills$getDamageShare();

	AntiFarmingPerEntity.State puffish_skills$getAntiFarmingPerEntityState();
}
