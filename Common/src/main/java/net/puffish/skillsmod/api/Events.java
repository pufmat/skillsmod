package net.puffish.skillsmod.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;

public class Events {

	public interface SkillUnlock {
		void onSkillUnlock(ServerPlayer player, Identifier categoryId, String skillId);
	}

	public interface SkillLock {
		void onSkillLock(ServerPlayer player, Identifier categoryId, String skillId);
	}

	public interface NewPoint {
		void onNewPoint(ServerPlayer player, Identifier categoryId);
	}

}
