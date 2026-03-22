package net.puffish.skillsmod.client.config;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;

public sealed interface ClientIconConfig permits ClientIconConfig.EffectIconConfig, ClientIconConfig.ItemIconConfig, ClientIconConfig.TextureIconConfig {

	record ItemIconConfig(ItemStack item) implements ClientIconConfig { }

	record EffectIconConfig(MobEffect effect) implements ClientIconConfig { }

	record TextureIconConfig(Identifier texture) implements ClientIconConfig { }

}
