package net.puffish.skillsmod.client.data;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public sealed interface ClientIconData permits ClientIconData.EffectIconData, ClientIconData.ItemIconData, ClientIconData.TextureIconData {

	non-sealed class ItemIconData implements ClientIconData {
		private final ItemStack item;

		public ItemIconData(ItemStack item) {
			this.item = item;
		}

		public ItemStack getItem() {
			return item;
		}
	}

	non-sealed class EffectIconData implements ClientIconData {
		private final StatusEffect effect;

		public EffectIconData(StatusEffect effect) {
			this.effect = effect;
		}

		public StatusEffect getEffect() {
			return effect;
		}
	}

	non-sealed class TextureIconData implements ClientIconData {
		private final Identifier texture;

		public TextureIconData(Identifier texture) {
			this.texture = texture;
		}

		public static TextureIconData createMissing() {
			return new TextureIconData(TextureManager.MISSING_IDENTIFIER);
		}

		public Identifier getTexture() {
			return texture;
		}
	}
}
