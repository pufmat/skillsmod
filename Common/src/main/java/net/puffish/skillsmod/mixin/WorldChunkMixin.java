package net.puffish.skillsmod.mixin;

import net.minecraft.world.chunk.WorldChunk;
import net.puffish.skillsmod.access.WorldChunkAccess;
import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements WorldChunkAccess {
	@Unique
	private final AntiFarmingPerChunk.State antiFarmingState = new AntiFarmingPerChunk.State();

	@Override
	public AntiFarmingPerChunk.State getAntiFarmingPerChunkState() {
		return antiFarmingState;
	}
}
