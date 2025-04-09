package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ConnectionBatchedRenderer {
	private final Int2ObjectMap<List<QuadEmit>> strokeBatch = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<List<QuadEmit>> fillBatch = new Int2ObjectOpenHashMap<>();

	private record QuadEmit(
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float x4, float y4, float z4
	) { }

	public void emitConnection(
			DrawContext context,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean bidirectional,
			int fillColor,
			int strokeColor
	) {
		var matrix = context.getMatrices().peek().getPositionMatrix();

		emitLine(strokeBatch, matrix, strokeColor, startX, startY, endX, endY, 3);
		if (!bidirectional) {
			emitArrow(strokeBatch, matrix, strokeColor, startX, startY, endX, endY, 8);
		}
		emitLine(fillBatch, matrix, fillColor, startX, startY, endX, endY, 1);
		if (!bidirectional) {
			emitArrow(fillBatch, matrix, fillColor, startX, startY, endX, endY, 6);
		}
	}

	private void emitLine(
			Int2ObjectMap<List<QuadEmit>> batch,
			Matrix4f matrix,
			int color,
			float startX,
			float startY,
			float endX,
			float endY,
			float thickness
	) {
		var side = new Vector2f(endX, endY)
				.sub(startX, startY)
				.normalize()
				.perpendicular()
				.mul(thickness / 2f);

		emitQuad(
				batch, matrix, color,
				startX + side.x, startY + side.y,
				startX - side.x, startY - side.y,
				endX - side.x, endY - side.y,
				endX + side.x, endY + side.y
		);
	}

	private void emitArrow(
			Int2ObjectMap<List<QuadEmit>> batch,
			Matrix4f matrix,
			int color,
			float startX,
			float startY,
			float endX,
			float endY,
			float thickness
	) {
		var center = new Vector2f(endX, endY)
				.add(startX, startY)
				.div(2f);
		var normal = new Vector2f(endX, endY)
				.sub(startX, startY)
				.normalize();
		var forward = new Vector2f(normal)
				.mul(thickness);
		var backward = new Vector2f(forward)
				.div(-2f);
		var back = new Vector2f(center)
				.add(backward);
		var side = new Vector2f(backward)
				.perpendicular()
				.mul(MathHelper.sqrt(3f));

		emitQuad(
				batch, matrix, color,
				center.x + forward.x, center.y + forward.y,
				back.x - side.x, back.y - side.y,
				back.x, back.y,
				back.x + side.x, back.y + side.y
		);
	}

	private void emitQuad(
			Int2ObjectMap<List<QuadEmit>> batch,
			Matrix4f matrix,
			int color,
			float x1, float y1,
			float x2, float y2,
			float x3, float y3,
			float x4, float y4
	) {
		var v1 = matrix.transformPosition(new Vector3f(x1, y1, 0f));
		var v2 = matrix.transformPosition(new Vector3f(x2, y2, 0f));
		var v3 = matrix.transformPosition(new Vector3f(x3, y3, 0f));
		var v4 = matrix.transformPosition(new Vector3f(x4, y4, 0f));

		var emits = batch.computeIfAbsent(color, key -> new ArrayList<>());

		emits.add(new QuadEmit(
				v1.x, v1.y, v1.z,
				v2.x, v2.y, v2.z,
				v3.x, v3.y, v3.z,
				v4.x, v4.y, v4.z
		));
	}

	public void draw(DrawContext context) {
		drawBatch(strokeBatch, context);
		drawBatch(fillBatch, context);
	}

	private void drawBatch(Int2ObjectMap<List<QuadEmit>> batch, DrawContext context) {
		for (var entry : batch.int2ObjectEntrySet()) {
			context.draw(vcp -> {
				var color = entry.getIntKey();
				var a = (float) ((color >> 24) & 0xff) / 255f;
				var r = (float) ((color >> 16) & 0xff) / 255f;
				var g = (float) ((color >> 8) & 0xff) / 255f;
				var b = (float) (color & 0xff) / 255f;
				RenderSystem.setShaderColor(r, g, b, a);

				var vc = vcp.getBuffer(RenderLayer.getGui());
				for (var emit : entry.getValue()) {
					vc.vertex(emit.x1, emit.y1, emit.z1).color(r, g, b, a);
					vc.vertex(emit.x2, emit.y2, emit.z2).color(r, g, b, a);
					vc.vertex(emit.x3, emit.y3, emit.z3).color(r, g, b, a);
					vc.vertex(emit.x4, emit.y4, emit.z4).color(r, g, b, a);
				}
			});
		}
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		batch.clear();
	}
}
