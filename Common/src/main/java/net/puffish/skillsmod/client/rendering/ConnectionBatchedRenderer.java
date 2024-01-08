package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class ConnectionBatchedRenderer {
	private final List<TriangleEmit> normalEmits = new ArrayList<>();
	private final List<TriangleEmit> exclusiveEmits = new ArrayList<>();
	private final List<TriangleEmit> outlineEmits = new ArrayList<>();

	private record TriangleEmit(
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3
	) { }

	public void emitNormalConnection(
			DrawContext context,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean bidirectional
	) {
		emitConnection(
				context,
				startX,
				startY,
				endX,
				endY,
				bidirectional,
				normalEmits
		);
	}

	public void emitExclusiveConnection(
			DrawContext context,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean bidirectional
	) {
		emitConnection(
				context,
				startX,
				startY,
				endX,
				endY,
				bidirectional,
				exclusiveEmits
		);
	}

	private void emitConnection(
			DrawContext context,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean bidirectional,
			List<TriangleEmit> emits
	) {
		var matrix = context.getMatrices().peek().getPositionMatrix();

		emitLine(matrix, outlineEmits, startX, startY, endX, endY, 3);
		if (!bidirectional) {
			emitArrow(matrix, outlineEmits, startX, startY, endX, endY, 8);
		}
		emitLine(matrix, emits, startX, startY, endX, endY, 1);
		if (!bidirectional) {
			emitArrow(matrix, emits, startX, startY, endX, endY, 6);
		}
	}

	private void emitLine(
			Matrix4f matrix,
			List<TriangleEmit> emits,
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

		emitTriangle(
				matrix, emits,
				startX + side.x, startY + side.y,
				startX - side.x, startY - side.y,
				endX + side.x, endY + side.y
		);
		emitTriangle(
				matrix, emits,
				endX - side.x, endY - side.y,
				endX + side.x, endY + side.y,
				startX - side.x, startY - side.y
		);
	}

	private void emitArrow(
			Matrix4f matrix,
			List<TriangleEmit> emits,
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

		emitTriangle(
				matrix, emits,
				center.x + forward.x, center.y + forward.y,
				back.x - side.x, back.y - side.y,
				back.x + side.x, back.y + side.y
		);
	}

	private void emitTriangle(
			Matrix4f matrix,
			List<TriangleEmit> emits,
			float x1, float y1,
			float x2, float y2,
			float x3, float y3
	) {
		var v1 = matrix.transform(new Vector4f(x1, y1, 0f, 1f));
		var v2 = matrix.transform(new Vector4f(x2, y2, 0f, 1f));
		var v3 = matrix.transform(new Vector4f(x3, y3, 0f, 1f));

		emits.add(new TriangleEmit(
				v1.x, v1.y, v1.z,
				v2.x, v2.y, v2.z,
				v3.x, v3.y, v3.z
		));
	}

	public void draw() {
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
		drawBatch(outlineEmits);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		drawBatch(normalEmits);
		RenderSystem.setShaderColor(1f, 0f, 0f, 1f);
		drawBatch(exclusiveEmits);
	}

	private void drawBatch(List<TriangleEmit> emits) {
		var bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
		for (var emit : emits) {
			bufferBuilder.vertex(emit.x1, emit.y1, emit.z1).next();
			bufferBuilder.vertex(emit.x2, emit.y2, emit.z2).next();
			bufferBuilder.vertex(emit.x3, emit.y3, emit.z3).next();
		}
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}
}
