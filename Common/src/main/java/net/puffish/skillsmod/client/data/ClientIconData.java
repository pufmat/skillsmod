package net.puffish.skillsmod.client.data;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;

public sealed interface ClientIconData permits ClientIconData.EffectIconData, ClientIconData.ItemIconData, ClientIconData.TextureIconData {

	non-sealed class ItemIconData implements ClientIconData {
		private final ItemStack item;

		private ItemIconData(ItemStack item) {
			this.item = item;
		}

		public static Result<ItemIconData, Failure> parse(JsonElementWrapper rootElement) {
			return JsonParseUtils.parseItemStack(rootElement).mapSuccess(ItemIconData::new);
		}

		public ItemStack getItem() {
			return item;
		}
	}

	non-sealed class EffectIconData implements ClientIconData {
		private final StatusEffect effect;

		private EffectIconData(StatusEffect effect) {
			this.effect = effect;
		}

		public static Result<EffectIconData, Failure> parse(JsonElementWrapper rootElement) {
			return rootElement
					.getAsObject()
					.andThen(rootObject -> rootObject.get("effect"))
					.andThen(JsonParseUtils::parseEffect)
					.mapSuccess(EffectIconData::new);
		}

		public StatusEffect getEffect() {
			return effect;
		}
	}

	non-sealed class TextureIconData implements ClientIconData {
		private final Identifier texture;

		private TextureIconData(Identifier texture) {
			this.texture = texture;
		}

		public static Result<TextureIconData, Failure> parse(JsonElementWrapper rootElement) {
			return rootElement
					.getAsObject()
					.andThen(rootObject -> rootObject.get("texture"))
					.andThen(JsonParseUtils::parseIdentifier)
					.mapSuccess(TextureIconData::new);
		}

		public static TextureIconData createMissing() {
			return new TextureIconData(TextureManager.MISSING_IDENTIFIER);
		}

		public Identifier getTexture() {
			return texture;
		}
	}
}
