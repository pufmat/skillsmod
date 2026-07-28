package net.puffish.skillsmod.client.config;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.common.BackgroundPosition;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.util.Optional;

public record ClientBackgroundConfig(
		Identifier texture,
		int width,
		int height,
		BackgroundPosition position
) {
	public static ClientBackgroundConfig create(
			Identifier textureId,
			int width,
			int height,
			BackgroundPosition position
	) {
		var id = SkillsMod.createIdentifier(RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789"));
		MinecraftClient.getInstance().execute(() -> {
			var texture = new ClientBackgroundTexture(textureId);
			MinecraftClient.getInstance()
					.getTextureManager()
					.registerTexture(id, texture);
		});

		return new ClientBackgroundConfig(
				id,
				width,
				height,
				position
		);
	}

	private static class ClientBackgroundTexture extends AbstractTexture implements TextureTickListener {
		private final Identifier id;
		private Sprite sprite;

		public ClientBackgroundTexture(Identifier id) {
			this.id = id;
		}

		@Override
		public void load(ResourceManager manager) {
			sprite = manager.getResource(id)
					.flatMap(resource -> {
						try {
							var metadata = resource.getMetadata()
									.decode(AnimationResourceMetadata.READER)
									.orElse(AnimationResourceMetadata.EMPTY);
							var image = NativeImage.read(resource.getInputStream());
							var size = metadata.ensureImageSize(image.getWidth(), image.getHeight());
							var info = new Sprite.Info(id, size.getFirst(), size.getSecond(), metadata);
							return Optional.of((Sprite) new ClientBackgroundSprite(info, image.getWidth(), image.getHeight(), image));
						} catch (IOException ignored) {
							return Optional.empty();
						}
					})
					.orElseGet(() -> MissingSprite.getMissingSprite(null, 0, 16, 16, 0, 0));

			RenderSystem.recordRenderCall(this::setup);
		}

		private void setup() {
			// close may be called before setup is queued
			if (sprite != null) {
				bindTexture();
				TextureUtil.prepareImage(this.getGlId(), 0, sprite.getWidth(), sprite.getHeight());
				sprite.upload();
			}
		}

		@Override
		public void tick() {
			bindTexture();
			var animation = sprite.getAnimation();
			if (animation != null) {
				animation.tick();
			}
		}

		@Override
		public void close() {
			sprite.close();
			sprite = null;

			super.close();
		}
	}

	private static class ClientBackgroundSprite extends Sprite {
		public ClientBackgroundSprite(Info info, int width, int height, NativeImage image) {
			super(null, info, 0, width, height, 0, 0, image);
		}
	}

	public void dispose() {
		MinecraftClient.getInstance().execute(() -> {
			MinecraftClient.getInstance()
					.getTextureManager()
					.destroyTexture(this.texture);
		});
	}
}
