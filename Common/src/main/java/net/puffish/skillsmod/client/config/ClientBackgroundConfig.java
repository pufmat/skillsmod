package net.puffish.skillsmod.client.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Animator;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.common.BackgroundPosition;
import org.apache.commons.lang3.RandomStringUtils;

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
		private final SpriteContents sprite;
		private final Animator animator;

		public ClientBackgroundTexture(Identifier id) {
			this.sprite = MinecraftClient.getInstance()
					.getResourceManager()
					.getResource(id)
					.flatMap(resource -> Optional.ofNullable(
							SpriteOpener.create(SpriteLoader.METADATA_SERIALIZERS).loadSprite(id, resource)
					))
					.orElseGet(MissingSprite::createSpriteContents);
			this.animator = sprite.createAnimator();
			this.glTexture = RenderSystem.getDevice().createTexture(id.toString(), TextureFormat.RGBA8, this.sprite.getWidth(), this.sprite.getHeight(), 1);

			sprite.upload(0, 0, glTexture);
		}

		@Override
		public void tick() {
			if (animator != null) {
				animator.tick(0, 0, glTexture);
			}
		}

		@Override
		public void close() {
			sprite.close();
			if (animator != null) {
				animator.close();
			}

			super.close();
		}
	}
}
