package net.puffish.skillsmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Vector2f;

public class DrawUtils {

	public static void drawTextWithBorder(
			MatrixStack matrices,
			Text text,
			int x,
			int y,
			int borderColor,
			int textColor
	) {
		var textRenderer = MinecraftClient.getInstance().textRenderer;
		textRenderer.draw(matrices, text, x - 1, y, borderColor);
		textRenderer.draw(matrices, text, x, y - 1, borderColor);
		textRenderer.draw(matrices, text, x + 1, y, borderColor);
		textRenderer.draw(matrices, text, x, y + 1, borderColor);
		textRenderer.draw(matrices, text, x, y, textColor);
	}

	public static void drawItem(
			MatrixStack matrices,
			int targetX,
			int targetY,
			ItemStack itemStack
	) {
		MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(
				matrices,
				itemStack,
				targetX,
				targetY
		);
	}

	public static void drawSingleTexture(
			MatrixStack matrices,
			int targetX,
			int targetY,
			int targetW,
			int targetH
	) {
		DrawableHelper.drawTexture(
				matrices,
				targetX,
				targetY,
				0,
				0,
				targetW,
				targetH,
				targetW,
				targetH
		);
	}

	public static void drawSingleSprite(
			MatrixStack matrices,
			int targetX,
			int targetY,
			int targetW,
			int targetH,
			Sprite sprite
	) {
		DrawableHelper.drawSprite(
				matrices,
				targetX,
				targetY,
				0,
				targetW,
				targetH,
				sprite
		);
	}

	public static void drawRepeatedTexture(
			MatrixStack matrices,
			int targetX,
			int targetY,
			int targetW,
			int targetH,
			int sourceX,
			int sourceY,
			int textureW,
			int textureH
	) {
		DrawableHelper.drawTexture(
				matrices,
				targetX,
				targetY,
				sourceX,
				sourceY,
				targetW,
				targetH,
				textureW,
				textureH
		);
	}

	public static void drawScaledTexture(
			MatrixStack matrices,
			int targetX,
			int targetY,
			int targetW,
			int targetH,
			int sourceX,
			int sourceY,
			int sourceW,
			int sourceH,
			int textureW,
			int textureH
	) {
		DrawableHelper.drawTexture(
				matrices,
				targetX,
				targetY,
				targetW,
				targetH,
				sourceX,
				sourceY,
				sourceW,
				sourceH,
				textureW,
				textureH
		);
	}

	public static void drawLine(
			MatrixStack matrices,
			int startX,
			int startY,
			int endX,
			int endY,
			int thickness,
			int color
	) {
		float a = ((float) (color >> 24 & 0xff)) / 255f;
		float r = ((float) (color >> 16 & 0xff)) / 255f;
		float g = ((float) (color >> 8 & 0xff)) / 255f;
		float b = ((float) (color & 0xff)) / 255f;
		var matrix = matrices.peek().getPositionMatrix();
		var tmp = new Vector2f(endX, endY)
				.sub(startX, startY)
				.normalize()
				.perpendicular()
				.mul(thickness / 2f);

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, startX + tmp.x, startY + tmp.y, 0).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, startX - tmp.x, startY - tmp.y, 0).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, endX - tmp.x, endY - tmp.y, 0).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, endX + tmp.x, endY + tmp.y, 0).color(r, g, b, a).next();
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}
}
