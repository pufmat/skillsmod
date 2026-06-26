package net.puffish.skillsmod.client.config;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.common.BackgroundPosition;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.system.MemoryUtil;

import java.util.Optional;
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
		var client = Minecraft.getInstance();
		client.execute(() -> client.getTextureManager()
				.register(id, new ClientBackgroundTexture(textureId)));

		return new ClientBackgroundConfig(
				id,
				width,
				height,
				position
		);
	}

	private static class ClientBackgroundSprite extends TextureAtlasSprite {
		private ClientBackgroundSprite(Identifier id, SpriteContents contents) {
			super(id, contents, contents.width(), contents.height(), 0, 0, 0);
		}
	}

	private static class ClientBackgroundTexture extends AbstractTexture implements TickableTexture {
		private final SpriteContents contents;
		private final SpriteContents.AnimationState animationState;
		private final GpuBuffer gpuBuffer;

		public ClientBackgroundTexture(Identifier id) {
			sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
			contents = Minecraft.getInstance()
					.getResourceManager()
					.getResource(id)
					.flatMap(resource -> Optional.ofNullable(
							SpriteResourceLoader.create(Set.of(GuiMetadataSection.TYPE)).loadSprite(id, resource)
					))
					.orElseGet(MissingTextureAtlasSprite::create);

			var size = Mth.roundToward(SpriteContents.UBO_SIZE, RenderSystem.getDevice().getDeviceInfo().limits().minUniformOffsetAlignment());
			var byteBuffer = MemoryUtil.memAlloc(size);

			var sprite = new ClientBackgroundSprite(id, contents);
			sprite.uploadSpriteUbo(byteBuffer, 0, 0, contents.width(), contents.height(), size);

			gpuBuffer = RenderSystem.getDevice().createBuffer(id::toString, GpuBuffer.USAGE_UNIFORM, byteBuffer);
			animationState = contents.createAnimationState(gpuBuffer.slice(), size);

			texture = RenderSystem.getDevice().createTexture(
					id.toString(),
					GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
					GpuFormat.RGBA8_UNORM,
					contents.width(),
					contents.height(),
					1,
					1
			);
			textureView = RenderSystem.getDevice().createTextureView(texture);

			if (!contents.isAnimated()) {
				contents.uploadFirstFrame(texture, 0);
			}
			if (animationState != null) {
				uploadAnimationFrames();
			}
		}

		private void uploadAnimationFrames() {
			try (var renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(() -> "Animate " + contents.name(), textureView, Optional.empty())) {
				RenderSystem.bindDefaultUniforms(renderPass);

				if (animationState.needsToDraw()) {
					animationState.drawToAtlas(renderPass, animationState.getDrawUbo(0));
				}
			}
		}

		@Override
		public void tick() {
			if (animationState != null) {
				animationState.tick();
				uploadAnimationFrames();
			}
		}

		@Override
		public void close() {
			gpuBuffer.close();
			contents.close();
			if (animationState != null) {
				animationState.close();
			}

			super.close();
		}
	}
}
