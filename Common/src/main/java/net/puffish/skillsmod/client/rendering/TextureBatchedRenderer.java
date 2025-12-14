package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Scaling;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.access.DrawContextAccess;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureBatchedRenderer {
	private final Map<Identifier, List<TextureEmit>> batch = new HashMap<>();

	private record TextureEmit(
			float x1, float y1,
			float x2, float y2,
			float x3, float y3,
			float x4, float y4,

			float minU, float minV, float maxU, float maxV,
			int color
	) { }

	public void emitTexture(
			DrawContext context, Identifier texture,
			int x, int y, int width, int height,
			int color
	) {
		emitTextureBatched(
				context,
				texture,
				x, y, x + width, y + height,
				0f, 0f, 1f, 1f,
				color
		);
	}

	public void emitSprite(
			DrawContext context, Sprite sprite, Scaling scaling,
			int x, int y, int width, int height,
			int color
	) {
		if (scaling instanceof Scaling.Stretch) {
			emitSpriteStretch(
					context, sprite,
					x, y, width, height,
					color
			);
		} else if (scaling instanceof Scaling.Tile tile) {
			emitSpriteTile(
					context, sprite, tile,
					x, y, width, height,
					color
			);
		} else if (scaling instanceof Scaling.NineSlice nineSlice) {
			emitSpriteNineSlice(
					context, sprite, nineSlice,
					x, y, width, height,
					color
			);
		}
	}

	private void emitSpriteTile(
			DrawContext context, Sprite sprite, Scaling.Tile tile,
			int x, int y, int width, int height,
			int color
	) {
		if (width <= 0 || height <= 0 || tile.width() <= 0 || tile.height() <= 0) {
			return;
		}
		for (var tileX = 0; tileX < width; tileX += tile.width()) {
			var tileWidth = Math.min(tile.width(), width - tileX);
			for (var tileY = 0; tileY < height; tileY += tile.height()) {
				var tileHeight = Math.min(tile.height(), height - tileY);
				emitSpriteStretch(
						context, sprite,
						x + tileX, y + tileY, tileWidth, tileHeight,
						color
				);
			}
		}
	}

	private void emitSpriteNineSlice(
			DrawContext context, Sprite sprite, Scaling.NineSlice nineSlice,
			int x, int y, int width, int height,
			int color
	) {
		if (width == nineSlice.width() && height == nineSlice.height()) {
			emitSpriteStretch(
					context, sprite,
					x, y, width, height,
					color
			);
			return;
		}

		var border = nineSlice.border();
		var left = Math.min(border.left(), width / 2);
		var top = Math.min(border.top(), height / 2);
		var right = Math.min(border.right(), width / 2);
		var bottom = Math.min(border.bottom(), height / 2);

		if (width == nineSlice.width()) {
			// top
			emitTextureBatched(
					context,
					sprite.getAtlasId(),
					x, y, x + width, y + top,
					sprite.getMinU(),
					sprite.getMinV(),
					sprite.getMaxU(),
					sprite.getFrameV((float) top / nineSlice.height()),
					color
			);

			// middle
			for (var tileY = top; tileY < height - bottom; tileY += nineSlice.height() - top - bottom) {
				var tileHeight = Math.min(nineSlice.height() - top - bottom, height - bottom - tileY);

				emitTextureBatched(
						context,
						sprite.getAtlasId(),
						x, y + tileY, x + nineSlice.width(), y + tileY + tileHeight,
						sprite.getMinU(),
						sprite.getFrameV((float) top / nineSlice.height()),
						sprite.getMaxU(),
						sprite.getFrameV((float) (top + tileHeight) / nineSlice.height()),
						color
				);
			}

			// bottom
			emitTextureBatched(
					context,
					sprite.getAtlasId(),
					x, y + height - bottom, x + width, y + height,
					sprite.getMinU(),
					sprite.getFrameV((float) (nineSlice.height() - bottom) / nineSlice.height()),
					sprite.getMaxU(),
					sprite.getMaxV(),
					color
			);
			return;
		}

		if (height == nineSlice.height()) {
			// left
			emitTextureBatched(
					context,
					sprite.getAtlasId(),
					x, y, x + left, y + height,
					sprite.getMinU(),
					sprite.getMinV(),
					sprite.getFrameU((float) left / nineSlice.width()),
					sprite.getMaxV(),
					color
			);

			// middle
			for (var tileX = left; tileX < width - right; tileX += nineSlice.width() - left - right) {
				var tileWidth = Math.min(nineSlice.width() - left - right, width - right - tileX);

				emitTextureBatched(
						context,
						sprite.getAtlasId(),
						x + tileX, y, x + tileX + tileWidth, y + nineSlice.height(),
						sprite.getFrameU((float) left / nineSlice.width()),
						sprite.getMinV(),
						sprite.getFrameU((float) (left + tileWidth) / nineSlice.width()),
						sprite.getMaxV(),
						color
				);
			}

			// right
			emitTextureBatched(
					context,
					sprite.getAtlasId(),
					x + width - right, y, x + width, y + height,
					sprite.getFrameU((float) (nineSlice.width() - right) / nineSlice.width()),
					sprite.getMinV(),
					sprite.getMaxU(),
					sprite.getMaxV(),
					color
			);
			return;
		}

		// top left
		emitTextureBatched(
				context,
				sprite.getAtlasId(),
				x, y, x + left, y + right,
				sprite.getMinU(),
				sprite.getMinV(),
				sprite.getFrameU((float) left / nineSlice.width()),
				sprite.getFrameV((float) right / nineSlice.width()),
				color
		);

		//top right
		emitTextureBatched(
				context,
				sprite.getAtlasId(),
				x + width - right, y, x + width, y + top,
				sprite.getFrameU((float) (nineSlice.width() - right) / nineSlice.width()),
				sprite.getMinV(),
				sprite.getMaxU(),
				sprite.getFrameV((float) top / nineSlice.height()),
				color
		);

		// bottom right
		emitTextureBatched(
				context,
				sprite.getAtlasId(),
				x + width - right, y + height - bottom, x + width, y + height,
				sprite.getFrameU((float) (nineSlice.width() - right) / nineSlice.width()),
				sprite.getFrameV((float) (nineSlice.height() - bottom) / nineSlice.height()),
				sprite.getMaxU(),
				sprite.getMaxV(),
				color
		);

		// bottom left
		emitTextureBatched(
				context,
				sprite.getAtlasId(),
				x, y + height - bottom, x + left, y + height,
				sprite.getMinU(),
				sprite.getFrameV((float) (nineSlice.height() - bottom) / nineSlice.height()),
				sprite.getFrameU((float) left / nineSlice.width()),
				sprite.getMaxV(),
				color
		);

		// top and bottom
		for (var tileX = left; tileX < width - right; tileX += nineSlice.width() - left - right) {
			var tileWidth = Math.min(nineSlice.width() - left - right, width - right - tileX);

			// top
			emitTextureBatched(
					context,
					sprite.getAtlasId(),
					x + tileX, y, x + tileX + tileWidth, y + top,
					sprite.getFrameU((float) left / nineSlice.width()),
					sprite.getMinV(),
					sprite.getFrameU((float) (left + tileWidth) / nineSlice.width()),
					sprite.getFrameV((float) top / nineSlice.height()),
					color
			);

			// bottom
			emitTextureBatched(
					context,
					sprite.getAtlasId(),
					x + tileX, y + height - bottom, x + tileX + tileWidth, y + height,
					sprite.getFrameU((float) left / nineSlice.width()),
					sprite.getFrameV((float) (nineSlice.height() - bottom) / nineSlice.height()),
					sprite.getFrameU((float) (left + tileWidth) / nineSlice.width()),
					sprite.getMaxV(),
					color
			);
		}

		// left and right
		for (var tileY = top; tileY < height - bottom; tileY += nineSlice.height() - top - bottom) {
			var tileHeight = Math.min(nineSlice.height() - top - bottom, height - bottom - tileY);

			// left
			emitTextureBatched(
					context,
					sprite.getAtlasId(),
					x, y + tileY, x + left, y + tileY + tileHeight,
					sprite.getMinU(),
					sprite.getFrameV((float) top / nineSlice.height()),
					sprite.getFrameU((float) left / nineSlice.width()),
					sprite.getFrameV((float) (top + tileHeight) / nineSlice.height()),
					color
			);

			// right
			emitTextureBatched(
					context,
					sprite.getAtlasId(),
					x + width - right, y + tileY, x + width, y + tileY + tileHeight,
					sprite.getFrameU((float) (nineSlice.width() - right) / nineSlice.width()),
					sprite.getFrameV((float) top / nineSlice.height()),
					sprite.getMaxU(),
					sprite.getFrameV((float) (top + tileHeight) / nineSlice.height()),
					color
			);
		}

		// middle
		for (var tileX = left; tileX < width - right; tileX += nineSlice.width() - left - right) {
			var tileWidth = Math.min(nineSlice.width() - left - right, width - right - tileX);

			for (var tileY = top; tileY < height - bottom; tileY += nineSlice.height() - top - bottom) {
				var tileHeight = Math.min(nineSlice.height() - top - bottom, height - bottom - tileY);

				emitTextureBatched(
						context,
						sprite.getAtlasId(),
						x + tileX, y + tileY, x + tileX + tileWidth, y + tileY + tileHeight,
						sprite.getFrameU((float) left / nineSlice.width()),
						sprite.getFrameV((float) top / nineSlice.height()),
						sprite.getFrameU((float) (left + tileWidth) / nineSlice.width()),
						sprite.getFrameV((float) (top + tileHeight) / nineSlice.height()),
						color
				);
			}
		}
	}

	private void emitSpriteStretch(
			DrawContext context, Sprite sprite,
			int x, int y, int width, int height,
			int color
	) {
		emitTextureBatched(
				context,
				sprite.getAtlasId(),
				x, y, x + width, y + height,
				sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(),
				color
		);
	}

	private void emitTextureBatched(
			DrawContext context, Identifier texture,
			float minX, float minY, float maxX, float maxY,
			float minU, float minV, float maxU, float maxV,
			int color
	) {
		var emits = batch.computeIfAbsent(texture, key -> new ArrayList<>());

		var matrix = context.getMatrices();

		var v1 = matrix.transformPosition(new Vector2f(minX, minY));
		var v2 = matrix.transformPosition(new Vector2f(minX, maxY));
		var v3 = matrix.transformPosition(new Vector2f(maxX, maxY));
		var v4 = matrix.transformPosition(new Vector2f(maxX, minY));

		emits.add(new TextureEmit(
				v1.x, v1.y,
				v2.x, v2.y,
				v3.x, v3.y,
				v4.x, v4.y,
				minU, minV, maxU, maxV,
				color
		));
	}

	public void draw(DrawContext context, TextureManager textureManager, ScreenRect scissorArea) {
		if (batch.isEmpty()) {
			return;
		}

		for (var entry : batch.entrySet()) {
			var texture = textureManager.getTexture(entry.getKey()).getGlTextureView();
			var emits = entry.getValue();
			var bounds = calcBounds(emits);
			var emitsCopy = List.copyOf(emits);

			var contextAccess = (DrawContextAccess) context;
			contextAccess.getState().addSimpleElement(new SimpleGuiElementRenderState() {
				@Override
				public void setupVertices(VertexConsumer vc) {
					for (var emit : emitsCopy) {
						vc.vertex(emit.x1, emit.y1, 0).texture(emit.minU, emit.minV).color(emit.color);
						vc.vertex(emit.x2, emit.y2, 0).texture(emit.minU, emit.maxV).color(emit.color);
						vc.vertex(emit.x3, emit.y3, 0).texture(emit.maxU, emit.maxV).color(emit.color);
						vc.vertex(emit.x4, emit.y4, 0).texture(emit.maxU, emit.minV).color(emit.color);
					}
				}

				@Override
				public RenderPipeline pipeline() {
					return RenderPipelines.GUI_TEXTURED;
				}

				@Override
				public TextureSetup textureSetup() {
					return TextureSetup.of(texture, RenderSystem.getSamplerCache().getRepeated(FilterMode.NEAREST));
				}

				@Override
				public ScreenRect scissorArea() {
					return scissorArea;
				}

				@Override
				public ScreenRect bounds() {
					return bounds;
				}
			});
		}

		batch.clear();
	}

	private static @NotNull ScreenRect calcBounds(List<TextureEmit> emits) {
		var minX = Float.POSITIVE_INFINITY;
		var minY = Float.POSITIVE_INFINITY;
		var maxX = Float.NEGATIVE_INFINITY;
		var maxY = Float.NEGATIVE_INFINITY;

		for (var emit : emits) {
			minX = Math.min(minX, emit.x1);
			minX = Math.min(minX, emit.x2);
			minX = Math.min(minX, emit.x3);
			minX = Math.min(minX, emit.x4);

			minY = Math.min(minY, emit.y1);
			minY = Math.min(minY, emit.y2);
			minY = Math.min(minY, emit.y3);
			minY = Math.min(minY, emit.y4);

			maxX = Math.max(maxX, emit.x1);
			maxX = Math.max(maxX, emit.x2);
			maxX = Math.max(maxX, emit.x3);
			maxX = Math.max(maxX, emit.x4);

			maxY = Math.max(maxY, emit.y1);
			maxY = Math.max(maxY, emit.y2);
			maxY = Math.max(maxY, emit.y3);
			maxY = Math.max(maxY, emit.y4);
		}

		return new ScreenRect(
				MathHelper.floor(minX),
				MathHelper.floor(minY),
				MathHelper.ceil(maxX - minX),
				MathHelper.ceil(maxY - minY)
		);
	}
}
