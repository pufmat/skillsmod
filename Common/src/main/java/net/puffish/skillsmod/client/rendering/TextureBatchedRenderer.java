package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureBatchedRenderer {
	private final Map<Identifier, List<TextureEmit>> batch = new HashMap<>();

	private record TextureEmit(
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float x4, float y4, float z4,

			float minU, float minV, float maxU, float maxV,
			Vector4fc color
	) { }

	public void emitTexture(
			DrawContext context, Identifier texture,
			int x, int y, int width, int height,
			Vector4fc color
	) {
		emitTextureBatched(
				context,
				texture,
				x, y, x + width, y + height,
				0f, 0f, 1f, 1f,
				color
		);
	}

	public void emitTexture(
			DrawContext context, Identifier texture,
			int x, int y, int width, int height,
			float minU, float minV, float maxU, float maxV,
			Vector4fc color
	) {
		emitTextureBatched(
				context,
				texture,
				x, y, x + width, y + height,
				minU, minV, maxU, maxV,
				color
		);
	}

	public void emitSpriteStretch(
			DrawContext context, Sprite sprite,
			int x, int y, int width, int height,
			Vector4fc color
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
			Vector4fc color
	) {
		var emits = batch.computeIfAbsent(texture, key -> new ArrayList<>());

		var matrix = context.getMatrices().peek().getPositionMatrix();

		var v1 = matrix.transform(new Vector4f(minX, minY, 0f, 1f));
		var v2 = matrix.transform(new Vector4f(minX, maxY, 0f, 1f));
		var v3 = matrix.transform(new Vector4f(maxX, maxY, 0f, 1f));
		var v4 = matrix.transform(new Vector4f(maxX, minY, 0f, 1f));

		emits.add(new TextureEmit(
				v1.x, v1.y, v1.z,
				v2.x, v2.y, v2.z,
				v3.x, v3.y, v3.z,
				v4.x, v4.y, v4.z,
				minU, minV, maxU, maxV,
				color
		));
	}

	public void draw() {
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
		for (var entry : batch.entrySet()) {
			RenderSystem.setShaderTexture(0, entry.getKey());
			var bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
			for (var emit : entry.getValue()) {
				bufferBuilder.vertex(emit.x1, emit.y1, emit.z1).color(emit.color.x(), emit.color.y(), emit.color.z(), emit.color.w()).texture(emit.minU, emit.minV).next();
				bufferBuilder.vertex(emit.x2, emit.y2, emit.z2).color(emit.color.x(), emit.color.y(), emit.color.z(), emit.color.w()).texture(emit.minU, emit.maxV).next();
				bufferBuilder.vertex(emit.x3, emit.y3, emit.z3).color(emit.color.x(), emit.color.y(), emit.color.z(), emit.color.w()).texture(emit.maxU, emit.maxV).next();
				bufferBuilder.vertex(emit.x4, emit.y4, emit.z4).color(emit.color.x(), emit.color.y(), emit.color.z(), emit.color.w()).texture(emit.maxU, emit.minV).next();
			}
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		}
		batch.clear();
	}
}
