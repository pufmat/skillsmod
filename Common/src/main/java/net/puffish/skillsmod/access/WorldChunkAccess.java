package net.puffish.skillsmod.access;

import net.puffish.skillsmod.experience.builtin.KillEntityExperienceSource;

public interface WorldChunkAccess {
	boolean antiFarmingAddAndCheck(KillEntityExperienceSource.AntiFarming antiFarming);

	void antiFarmingCleanupOutdated();
}
