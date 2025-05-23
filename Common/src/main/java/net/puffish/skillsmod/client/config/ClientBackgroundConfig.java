package net.puffish.skillsmod.client.config;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Animator;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.resource.ResourceManager;
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
		var texture = new ClientBackgroundTexture(textureId);
		MinecraftClient.getInstance()
				.getTextureManager()
				.registerTexture(id, texture);

		return new ClientBackgroundConfig(
				id,
				width,
				height,
				position
		);
	}

	private static class ClientBackgroundTexture extends AbstractTexture implements TextureTickListener {
		private final Identifier id;
		private SpriteContents sprite;
		private Animator animator;

		public ClientBackgroundTexture(Identifier id) {
			this.id = id;
		}

		@Override
		public void load(ResourceManager manager) {
			sprite = manager
					.getResource(id)
					.flatMap(resource -> Optional.ofNullable(SpriteLoader.load(id, resource)))
					.orElseGet(MissingSprite::createSpriteContents);
			animator = sprite.createAnimator();

			RenderSystem.recordRenderCall(() -> {
				bindTexture();
				TextureUtil.prepareImage(this.getGlId(), 0, sprite.getWidth(), sprite.getHeight());
				sprite.upload(0, 0);
			});
		}

		@Override
		public void tick() {
			bindTexture();
			if (animator != null) {
				animator.tick(0, 0);
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
