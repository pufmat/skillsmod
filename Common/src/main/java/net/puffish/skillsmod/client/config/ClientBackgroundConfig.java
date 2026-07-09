package net.puffish.skillsmod.client.config;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.GuiResourceMetadata;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.common.BackgroundPosition;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.system.MemoryUtil;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

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
		var id = SkillsMod.createIdentifier(RandomStringUtils.insecure()
				.next(16, "abcdefghijklmnopqrstuvwxyz0123456789"));
		var client = MinecraftClient.getInstance();
		client.execute(() -> client.getTextureManager()
				.registerTexture(id, new ClientBackgroundTexture(textureId)));

		return new ClientBackgroundConfig(
				id,
				width,
				height,
				position
		);
	}

	private static class ClientBackgroundSprite extends Sprite {
		private ClientBackgroundSprite(Identifier id, SpriteContents contents) {
			super(id, contents, contents.getWidth(), contents.getHeight(), 0, 0, 0);
		}
	}

	private static class ClientBackgroundTexture extends AbstractTexture implements TextureTickListener {
		private final SpriteContents contents;
		private final SpriteContents.Animator animator;
		private final GpuBuffer gpuBuffer;

		public ClientBackgroundTexture(Identifier id) {
			sampler = RenderSystem.getSamplerCache().getRepeated(FilterMode.NEAREST);
			contents = MinecraftClient.getInstance()
					.getResourceManager()
					.getResource(id)
					.flatMap(resource -> Optional.ofNullable(
							SpriteOpener.create(Set.of(GuiResourceMetadata.SERIALIZER)).loadSprite(id, resource)
					))
					.orElseGet(MissingSprite::createSpriteContents);

			var size = MathHelper.roundUpToMultiple(SpriteContents.SPRITE_INFO_SIZE, RenderSystem.getDevice().getUniformOffsetAlignment());
			var byteBuffer = MemoryUtil.memAlloc(size);

			var sprite = new ClientBackgroundSprite(id, contents);
			sprite.putSpriteInfo(byteBuffer, 0, 0, contents.getWidth(), contents.getHeight(), size);

			gpuBuffer = RenderSystem.getDevice().createBuffer(id::toString, GpuBuffer.USAGE_UNIFORM, byteBuffer);
			animator = contents.createAnimator(gpuBuffer.slice(), size);

			glTexture = RenderSystem.getDevice().createTexture(
					id.toString(),
					GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
					TextureFormat.RGBA8,
					contents.getWidth(),
					contents.getHeight(),
					1,
					1
			);
			glTextureView = RenderSystem.getDevice().createTextureView(glTexture);

			contents.upload(glTexture, 0);
		}

		@Override
		public void tick() {
			if (animator != null) {
				animator.tick();
				try (var renderPass = RenderSystem.getDevice()
						.createCommandEncoder()
						.createRenderPass(() -> "Animate " + contents.getId(), glTextureView, OptionalInt.empty())) {
					if (animator.isDirty()) {
						animator.upload(renderPass, animator.getBufferSlice(0));
					}
				}
			}
		}

		@Override
		public void close() {
			gpuBuffer.close();
			contents.close();
			if (animator != null) {
				animator.close();
			}

			super.close();
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
