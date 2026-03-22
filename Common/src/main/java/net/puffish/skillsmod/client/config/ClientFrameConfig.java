package net.puffish.skillsmod.client.config;

import java.util.Optional;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.resources.Identifier;

public sealed interface ClientFrameConfig permits ClientFrameConfig.AdvancementFrameConfig, ClientFrameConfig.TextureFrameConfig {

	record AdvancementFrameConfig(AdvancementType frame) implements ClientFrameConfig { }

	record TextureFrameConfig(
			Optional<Identifier> lockedTexture,
			Identifier availableTexture,
			Optional<Identifier> affordableTexture,
			Identifier unlockedTexture,
			Optional<Identifier> excludedTexture
	) implements ClientFrameConfig { }

}
