package net.puffish.skillsmod.mixin;

import net.minecraft.world.level.chunk.LevelChunk;
import net.puffish.skillsmod.access.LevelChunkAccess;
import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements LevelChunkAccess {
	@Unique
	private final AntiFarmingPerChunk.State antiFarmingState = new AntiFarmingPerChunk.State();

	@Override
	public AntiFarmingPerChunk.State getAntiFarmingPerChunkState() {
		return antiFarmingState;
	}
}
