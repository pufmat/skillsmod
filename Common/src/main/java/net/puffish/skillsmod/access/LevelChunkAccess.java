package net.puffish.skillsmod.access;

import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerChunk;

public interface LevelChunkAccess {
	AntiFarmingPerChunk.State puffish_skills$getAntiFarmingPerChunkState();
}
