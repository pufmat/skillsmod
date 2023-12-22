package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class ConnectionBatchedRenderer {
	private final BufferBuilder bufferBuilderNormal = new BufferBuilder(256);
	private final BufferBuilder bufferBuilderExclusive = new BufferBuilder(256);
	private final BufferBuilder bufferBuilderOutline = new BufferBuilder(256);

	public ConnectionBatchedRenderer() {
		bufferBuilderNormal.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
		bufferBuilderExclusive.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
		bufferBuilderOutline.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
	}

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
				bufferBuilderNormal
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
				bufferBuilderExclusive
		);
	}

	public void emitConnection(
			DrawContext context,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean bidirectional,
			BufferBuilder bufferBuilder
	) {
		var matrix = context.getMatrices().peek().getPositionMatrix();

		emitLine(matrix, bufferBuilderOutline, startX, startY, endX, endY, 3);
		if (!bidirectional) {
			emitArrow(matrix, bufferBuilderOutline, startX, startY, endX, endY, 8);
		}
		emitLine(matrix, bufferBuilder, startX, startY, endX, endY, 1);
		if (!bidirectional) {
			emitArrow(matrix, bufferBuilder, startX, startY, endX, endY, 6);
		}
	}

	private void emitLine(
			Matrix4f matrix,
			BufferBuilder bufferBuilder,
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

		bufferBuilder.vertex(matrix, startX + side.x, startY + side.y, 0).next();
		bufferBuilder.vertex(matrix, startX - side.x, startY - side.y, 0).next();
		bufferBuilder.vertex(matrix, endX + side.x, endY + side.y, 0).next();

		bufferBuilder.vertex(matrix, endX - side.x, endY - side.y, 0).next();
		bufferBuilder.vertex(matrix, endX + side.x, endY + side.y, 0).next();
		bufferBuilder.vertex(matrix, startX - side.x, startY - side.y, 0).next();
	}

	private void emitArrow(
			Matrix4f matrix,
			BufferBuilder bufferBuilder,
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

		bufferBuilder.vertex(matrix, center.x + forward.x, center.y + forward.y, 0).next();
		bufferBuilder.vertex(matrix, back.x - side.x, back.y - side.y, 0).next();
		bufferBuilder.vertex(matrix, back.x + side.x, back.y + side.y, 0).next();
	}

	public void draw() {
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilderOutline.end());
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilderNormal.end());
		RenderSystem.setShaderColor(1f, 0f, 0f, 1f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilderExclusive.end());
		RenderSystem.applyModelViewMatrix();
	}
}
