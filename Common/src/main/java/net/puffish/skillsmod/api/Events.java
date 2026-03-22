package net.puffish.skillsmod.api;

import net.minecraft.resources.Identifier;

public class Events {

	public interface SkillUnlock {
		void onSkillUnlock(Identifier categoryId, String skillId);
	}

	public interface SkillLock {
		void onSkillLock(Identifier categoryId, String skillId);
	}

}
