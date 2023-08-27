package net.puffish.skillsmod.mixin;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.world.chunk.WorldChunk;
import net.puffish.skillsmod.access.WorldChunkAccess;
import net.puffish.skillsmod.experience.builtin.KillEntityExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements WorldChunkAccess {
	@Unique
	private final Map<KillEntityExperienceSource.AntiFarming, LongList> antiFarmingData = new HashMap<>();

	@Override
	@Unique
	public boolean antiFarmingAddAndCheck(KillEntityExperienceSource.AntiFarming antiFarming) {
		var data = antiFarmingData.computeIfAbsent(antiFarming, key -> new LongArrayList());

		if (data.size() < antiFarming.limitPerChunk()) {
			data.add(System.currentTimeMillis() + antiFarming.resetAfterSeconds() * 1000L);
			return true;
		}

		return false;
	}

	@Override
	@Unique
	public void antiFarmingCleanupOutdated() {
		var currentTime = System.currentTimeMillis();

		antiFarmingData.values().removeIf(data -> {
			data.removeIf(time -> time < currentTime);
			return data.isEmpty();
		});
	}
}
