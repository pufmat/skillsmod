package net.puffish.skillsmod.client.data;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;

public sealed interface ClientFrameData permits ClientFrameData.AdvancementFrameData, ClientFrameData.TextureFrameData {

	non-sealed class AdvancementFrameData implements ClientFrameData {
		private final AdvancementFrame frame;

		private AdvancementFrameData(AdvancementFrame frame) {
			this.frame = frame;
		}

		public static Result<AdvancementFrameData, Failure> parse(JsonElementWrapper rootElement) {
			return rootElement
					.getAsObject()
					.andThen(rootObject -> rootObject.get("frame"))
					.andThen(JsonParseUtils::parseFrame)
					.mapSuccess(AdvancementFrameData::new);
		}

		public AdvancementFrame getFrame() {
			return frame;
		}
	}

	non-sealed class TextureFrameData implements ClientFrameData {
		private final Identifier availableTexture;
		private final Identifier unlockedTexture;
		private final Identifier lockedTexture;
		private final Identifier excludedTexture;

		public TextureFrameData(Identifier availableTexture, Identifier unlockedTexture, Identifier lockedTexture, Identifier excludedTexture) {
			this.availableTexture = availableTexture;
			this.unlockedTexture = unlockedTexture;
			this.lockedTexture = lockedTexture;
			this.excludedTexture = excludedTexture;
		}

		public static Result<TextureFrameData, Failure> parse(JsonElementWrapper rootElement) {
			return rootElement.getAsObject().andThen(TextureFrameData::parse);
		}

		private static Result<TextureFrameData, Failure> parse(JsonObjectWrapper rootObject) {
			var failures = new ArrayList<Failure>();

			var optAvailableTexture = rootObject.get("available")
					.andThen(JsonParseUtils::parseIdentifier)
					.ifFailure(failures::add)
					.getSuccess();

			var optUnlockedTexture = rootObject.get("unlocked")
					.andThen(JsonParseUtils::parseIdentifier)
					.ifFailure(failures::add)
					.getSuccess();

			var lockedTexture = rootObject.get("locked")
					.andThen(JsonParseUtils::parseIdentifier)
					.getSuccess()
					.orElse(null);

			var excludedTexture = rootObject.get("excluded")
					.andThen(JsonParseUtils::parseIdentifier)
					.getSuccess()
					.orElse(null);

			if (failures.isEmpty()) {
				return Result.success(new TextureFrameData(
						optAvailableTexture.orElseThrow(),
						optUnlockedTexture.orElseThrow(),
						lockedTexture,
						excludedTexture
				));
			} else {
				return Result.failure(Failure.fromMany(failures));
			}
		}

		public static TextureFrameData createMissing() {
			return new TextureFrameData(
					TextureManager.MISSING_IDENTIFIER,
					TextureManager.MISSING_IDENTIFIER,
					TextureManager.MISSING_IDENTIFIER,
					TextureManager.MISSING_IDENTIFIER
			);
		}

		public Identifier getLockedTexture() {
			return lockedTexture;
		}

		public Identifier getAvailableTexture() {
			return availableTexture;
		}

		public Identifier getUnlockedTexture() {
			return unlockedTexture;
		}

		public Identifier getExcludedTexture() {
			return excludedTexture;
		}
	}
}
