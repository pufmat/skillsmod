package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.puffish.skillsmod.access.GuiGraphicsAccess;
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
			GuiGraphics graphics, Identifier texture,
			int x, int y, int width, int height,
			int color
	) {
		emitTextureBatched(
				graphics,
				texture,
				x, y, x + width, y + height,
				0f, 0f, 1f, 1f,
				color
		);
	}

	public void emitSprite(
			GuiGraphics graphics, TextureAtlasSprite sprite, GuiSpriteScaling scaling,
			int x, int y, int width, int height,
			int color
	) {
		if (scaling instanceof GuiSpriteScaling.Stretch) {
			emitSpriteStretch(
					graphics, sprite,
					x, y, width, height,
					color
			);
		} else if (scaling instanceof GuiSpriteScaling.Tile tile) {
			emitSpriteTile(
					graphics, sprite, tile,
					x, y, width, height,
					color
			);
		} else if (scaling instanceof GuiSpriteScaling.NineSlice nineSlice) {
			emitSpriteNineSlice(
					graphics, sprite, nineSlice,
					x, y, width, height,
					color
			);
		}
	}

	private void emitSpriteTile(
			GuiGraphics graphics, TextureAtlasSprite sprite, GuiSpriteScaling.Tile tile,
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
						graphics, sprite,
						x + tileX, y + tileY, tileWidth, tileHeight,
						color
				);
			}
		}
	}

	private void emitSpriteNineSlice(
			GuiGraphics graphics, TextureAtlasSprite sprite, GuiSpriteScaling.NineSlice nineSlice,
			int x, int y, int width, int height,
			int color
	) {
		if (width == nineSlice.width() && height == nineSlice.height()) {
			emitSpriteStretch(
					graphics, sprite,
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
					graphics,
					sprite.atlasLocation(),
					x, y, x + width, y + top,
					sprite.getU0(),
					sprite.getV0(),
					sprite.getU1(),
					sprite.getV((float) top / nineSlice.height()),
					color
			);

			// middle
			for (var tileY = top; tileY < height - bottom; tileY += nineSlice.height() - top - bottom) {
				var tileHeight = Math.min(nineSlice.height() - top - bottom, height - bottom - tileY);

				emitTextureBatched(
						graphics,
						sprite.atlasLocation(),
						x, y + tileY, x + nineSlice.width(), y + tileY + tileHeight,
						sprite.getU0(),
						sprite.getV((float) top / nineSlice.height()),
						sprite.getU1(),
						sprite.getV((float) (top + tileHeight) / nineSlice.height()),
						color
				);
			}

			// bottom
			emitTextureBatched(
					graphics,
					sprite.atlasLocation(),
					x, y + height - bottom, x + width, y + height,
					sprite.getU0(),
					sprite.getV((float) (nineSlice.height() - bottom) / nineSlice.height()),
					sprite.getU1(),
					sprite.getV1(),
					color
			);
			return;
		}

		if (height == nineSlice.height()) {
			// left
			emitTextureBatched(
					graphics,
					sprite.atlasLocation(),
					x, y, x + left, y + height,
					sprite.getU0(),
					sprite.getV0(),
					sprite.getU((float) left / nineSlice.width()),
					sprite.getV1(),
					color
			);

			// middle
			for (var tileX = left; tileX < width - right; tileX += nineSlice.width() - left - right) {
				var tileWidth = Math.min(nineSlice.width() - left - right, width - right - tileX);

				emitTextureBatched(
						graphics,
						sprite.atlasLocation(),
						x + tileX, y, x + tileX + tileWidth, y + nineSlice.height(),
						sprite.getU((float) left / nineSlice.width()),
						sprite.getV0(),
						sprite.getU((float) (left + tileWidth) / nineSlice.width()),
						sprite.getV1(),
						color
				);
			}

			// right
			emitTextureBatched(
					graphics,
					sprite.atlasLocation(),
					x + width - right, y, x + width, y + height,
					sprite.getU((float) (nineSlice.width() - right) / nineSlice.width()),
					sprite.getV0(),
					sprite.getU1(),
					sprite.getV1(),
					color
			);
			return;
		}

		// top left
		emitTextureBatched(
				graphics,
				sprite.atlasLocation(),
				x, y, x + left, y + right,
				sprite.getU0(),
				sprite.getV0(),
				sprite.getU((float) left / nineSlice.width()),
				sprite.getV((float) right / nineSlice.width()),
				color
		);

		//top right
		emitTextureBatched(
				graphics,
				sprite.atlasLocation(),
				x + width - right, y, x + width, y + top,
				sprite.getU((float) (nineSlice.width() - right) / nineSlice.width()),
				sprite.getV0(),
				sprite.getU1(),
				sprite.getV((float) top / nineSlice.height()),
				color
		);

		// bottom right
		emitTextureBatched(
				graphics,
				sprite.atlasLocation(),
				x + width - right, y + height - bottom, x + width, y + height,
				sprite.getU((float) (nineSlice.width() - right) / nineSlice.width()),
				sprite.getV((float) (nineSlice.height() - bottom) / nineSlice.height()),
				sprite.getU1(),
				sprite.getV1(),
				color
		);

		// bottom left
		emitTextureBatched(
				graphics,
				sprite.atlasLocation(),
				x, y + height - bottom, x + left, y + height,
				sprite.getU0(),
				sprite.getV((float) (nineSlice.height() - bottom) / nineSlice.height()),
				sprite.getU((float) left / nineSlice.width()),
				sprite.getV1(),
				color
		);

		// top and bottom
		for (var tileX = left; tileX < width - right; tileX += nineSlice.width() - left - right) {
			var tileWidth = Math.min(nineSlice.width() - left - right, width - right - tileX);

			// top
			emitTextureBatched(
					graphics,
					sprite.atlasLocation(),
					x + tileX, y, x + tileX + tileWidth, y + top,
					sprite.getU((float) left / nineSlice.width()),
					sprite.getV0(),
					sprite.getU((float) (left + tileWidth) / nineSlice.width()),
					sprite.getV((float) top / nineSlice.height()),
					color
			);

			// bottom
			emitTextureBatched(
					graphics,
					sprite.atlasLocation(),
					x + tileX, y + height - bottom, x + tileX + tileWidth, y + height,
					sprite.getU((float) left / nineSlice.width()),
					sprite.getV((float) (nineSlice.height() - bottom) / nineSlice.height()),
					sprite.getU((float) (left + tileWidth) / nineSlice.width()),
					sprite.getV1(),
					color
			);
		}

		// left and right
		for (var tileY = top; tileY < height - bottom; tileY += nineSlice.height() - top - bottom) {
			var tileHeight = Math.min(nineSlice.height() - top - bottom, height - bottom - tileY);

			// left
			emitTextureBatched(
					graphics,
					sprite.atlasLocation(),
					x, y + tileY, x + left, y + tileY + tileHeight,
					sprite.getU0(),
					sprite.getV((float) top / nineSlice.height()),
					sprite.getU((float) left / nineSlice.width()),
					sprite.getV((float) (top + tileHeight) / nineSlice.height()),
					color
			);

			// right
			emitTextureBatched(
					graphics,
					sprite.atlasLocation(),
					x + width - right, y + tileY, x + width, y + tileY + tileHeight,
					sprite.getU((float) (nineSlice.width() - right) / nineSlice.width()),
					sprite.getV((float) top / nineSlice.height()),
					sprite.getU1(),
					sprite.getV((float) (top + tileHeight) / nineSlice.height()),
					color
			);
		}

		// middle
		for (var tileX = left; tileX < width - right; tileX += nineSlice.width() - left - right) {
			var tileWidth = Math.min(nineSlice.width() - left - right, width - right - tileX);

			for (var tileY = top; tileY < height - bottom; tileY += nineSlice.height() - top - bottom) {
				var tileHeight = Math.min(nineSlice.height() - top - bottom, height - bottom - tileY);

				emitTextureBatched(
						graphics,
						sprite.atlasLocation(),
						x + tileX, y + tileY, x + tileX + tileWidth, y + tileY + tileHeight,
						sprite.getU((float) left / nineSlice.width()),
						sprite.getV((float) top / nineSlice.height()),
						sprite.getU((float) (left + tileWidth) / nineSlice.width()),
						sprite.getV((float) (top + tileHeight) / nineSlice.height()),
						color
				);
			}
		}
	}

	private void emitSpriteStretch(
			GuiGraphics graphics, TextureAtlasSprite sprite,
			int x, int y, int width, int height,
			int color
	) {
		emitTextureBatched(
				graphics,
				sprite.atlasLocation(),
				x, y, x + width, y + height,
				sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
				color
		);
	}

	private void emitTextureBatched(
			GuiGraphics graphics, Identifier texture,
			float minX, float minY, float maxX, float maxY,
			float minU, float minV, float maxU, float maxV,
			int color
	) {
		var emits = batch.computeIfAbsent(texture, key -> new ArrayList<>());

		var matrix = graphics.pose();

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

	public void draw(GuiGraphics graphics, TextureManager textureManager, ScreenRectangle scissorArea) {
		if (batch.isEmpty()) {
			return;
		}

		for (var entry : batch.entrySet()) {
			var texture = textureManager.getTexture(entry.getKey()).getTextureView();
			var emits = entry.getValue();
			var bounds = calcBounds(emits);
			var emitsCopy = List.copyOf(emits);

			var graphicsAccess = (GuiGraphicsAccess) graphics;
			graphicsAccess.getState().submitGuiElement(new GuiElementRenderState() {
				@Override
				public void buildVertices(VertexConsumer vc) {
					for (var emit : emitsCopy) {
						vc.addVertex(emit.x1, emit.y1, 0).setUv(emit.minU, emit.minV).setColor(emit.color);
						vc.addVertex(emit.x2, emit.y2, 0).setUv(emit.minU, emit.maxV).setColor(emit.color);
						vc.addVertex(emit.x3, emit.y3, 0).setUv(emit.maxU, emit.maxV).setColor(emit.color);
						vc.addVertex(emit.x4, emit.y4, 0).setUv(emit.maxU, emit.minV).setColor(emit.color);
					}
				}

				@Override
				public RenderPipeline pipeline() {
					return RenderPipelines.GUI_TEXTURED;
				}

				@Override
				public TextureSetup textureSetup() {
					return TextureSetup.singleTexture(texture, RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
				}

				@Override
				public ScreenRectangle scissorArea() {
					return scissorArea;
				}

				@Override
				public ScreenRectangle bounds() {
					return bounds;
				}
			});
		}

		batch.clear();
	}

	private static @NotNull ScreenRectangle calcBounds(List<TextureEmit> emits) {
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

		return new ScreenRectangle(
				Mth.floor(minX),
				Mth.floor(minY),
				Mth.ceil(maxX - minX),
				Mth.ceil(maxY - minY)
		);
	}
}
