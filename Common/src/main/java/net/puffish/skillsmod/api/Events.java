package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class Events {

	public interface SkillUnlock {
		void onSkillUnlock(ServerPlayerEntity player, Identifier categoryId, String skillId);
	}

	public interface SkillLock {
		void onSkillLock(ServerPlayerEntity player, Identifier categoryId, String skillId);
	}

}
