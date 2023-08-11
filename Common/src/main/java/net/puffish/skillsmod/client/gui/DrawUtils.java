package net.puffish.skillsmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

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
		var matrices2 = RenderSystem.getModelViewStack();
		matrices2.push();
		matrices2.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
		RenderSystem.applyModelViewMatrix();
		MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(
				itemStack,
				targetX,
				targetY
		);
		matrices2.pop();
		RenderSystem.applyModelViewMatrix();
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
}
