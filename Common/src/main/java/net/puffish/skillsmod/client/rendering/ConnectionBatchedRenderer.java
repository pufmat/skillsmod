package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.access.DrawContextAccess;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class ConnectionBatchedRenderer {
	private final List<QuadEmit> strokeBatch = new ArrayList<>();
	private final List<QuadEmit> fillBatch = new ArrayList<>();

	private record QuadEmit(
			float x1, float y1,
			float x2, float y2,
			float x3, float y3,
			float x4, float y4,
			int color
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
		var matrix = context.getMatrices();

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
			List<QuadEmit> batch,
			Matrix3x2f matrix,
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
			List<QuadEmit> batch,
			Matrix3x2f matrix,
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
			List<QuadEmit> batch,
			Matrix3x2f matrix,
			int color,
			float x1, float y1,
			float x2, float y2,
			float x3, float y3,
			float x4, float y4
	) {
		var v1 = matrix.transformPosition(new Vector2f(x1, y1));
		var v2 = matrix.transformPosition(new Vector2f(x2, y2));
		var v3 = matrix.transformPosition(new Vector2f(x3, y3));
		var v4 = matrix.transformPosition(new Vector2f(x4, y4));

		batch.add(new QuadEmit(
				v1.x, v1.y,
				v2.x, v2.y,
				v3.x, v3.y,
				v4.x, v4.y,
				color
		));
	}

	public void draw(DrawContext context, ScreenRect scissorArea) {
		drawBatch(context, strokeBatch, scissorArea);
		drawBatch(context, fillBatch, scissorArea);
	}

	private void drawBatch(DrawContext context, List<QuadEmit> batch, ScreenRect scissorArea) {
		if (batch.isEmpty()) {
			return;
		}

		var bounds = calcBounds(batch);
		var batchCopy = List.copyOf(batch);
		batch.clear();

		var contextAccess = (DrawContextAccess) context;
		contextAccess.getState().addSimpleElement(new SimpleGuiElementRenderState() {
			@Override
			public void setupVertices(VertexConsumer vc) {
				for (var emit : batchCopy) {
					vc.vertex(emit.x1, emit.y1, 0).color(emit.color());
					vc.vertex(emit.x2, emit.y2, 0).color(emit.color());
					vc.vertex(emit.x3, emit.y3, 0).color(emit.color());
					vc.vertex(emit.x4, emit.y4, 0).color(emit.color());
				}
			}

			@Override
			public RenderPipeline pipeline() {
				return RenderPipelines.GUI;
			}

			@Override
			public TextureSetup textureSetup() {
				return TextureSetup.empty();
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

	private static @NotNull ScreenRect calcBounds(List<@NotNull QuadEmit> batchCopy) {
		var minX = Float.POSITIVE_INFINITY;
		var minY = Float.POSITIVE_INFINITY;
		var maxX = Float.NEGATIVE_INFINITY;
		var maxY = Float.NEGATIVE_INFINITY;

		for (var emit : batchCopy) {
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
