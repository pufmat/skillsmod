package net.puffish.skillsmod.access;

import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerChunk;

public interface WorldChunkAccess {
	AntiFarmingPerChunk.State getAntiFarmingPerChunkState();
}
