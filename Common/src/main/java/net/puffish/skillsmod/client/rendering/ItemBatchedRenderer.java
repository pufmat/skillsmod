package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.OversizedItemRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.pip.OversizedItemRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.access.GameRendererAccess;
import net.puffish.skillsmod.access.GuiGraphicsExtractorAccess;
import net.puffish.skillsmod.access.GuiRendererAccess;
import net.puffish.skillsmod.mixin.GuiRendererInvoker;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBatchedRenderer {

	private final Map<ComparableItemStack, List<Matrix3x2f>> batch = new HashMap<>();

	private static final Object KEY = new Object();

	public void emitItem(GuiGraphicsExtractor graphics, ItemStack item, int x, int y) {
		var emits = batch.computeIfAbsent(
				new ComparableItemStack(item),
				key -> new ArrayList<>()
		);

		emits.add(new Matrix3x2f(graphics.pose()).translate(x - 8, y - 8));
	}

	public void draw(GuiGraphicsExtractor graphics, ScreenRectangle scissorArea) {
		var client = Minecraft.getInstance();
		var gameRenderer = client.gameRenderer;
		var gameRendererAccess = (GameRendererAccess) gameRenderer;
		var guiRendererAccess = (GuiRendererAccess) gameRendererAccess.getGuiRenderer();
		var guiRendererInvoker = (GuiRendererInvoker) gameRendererAccess.getGuiRenderer();
		var graphicsAccess = (GuiGraphicsExtractorAccess) graphics;
		var guiRenderState = graphicsAccess.getGuiRenderState();
		var windowScaleFactor = guiRendererInvoker.invokeGetGuiScaleInvalidatingItemAtlasIfChanged();
		var featureRenderDispatcher = guiRendererAccess.getFeatureRenderDispatcher();

		for (var entry : batch.entrySet()) {
			var itemStack = entry.getKey().itemStack;

			var itemRenderState = new TrackingItemStackRenderState();
			client.getItemModelResolver().updateForTopItem(
					itemRenderState,
					itemStack,
					ItemDisplayContext.GUI,
					client.level,
					client.player,
					0
			);

			itemRenderState.appendModelIdentityElement(KEY);

			var renderer = guiRendererAccess.getOversizedItemRenderers().computeIfAbsent(
					itemRenderState.getModelIdentity(),
					object -> new ItemGuiElementRenderer()
			);

			for (var matrix : entry.getValue()) {
				var renderState = new GuiItemRenderState(
						matrix,
						itemRenderState,
						0,
						0,
						scissorArea
				);
				renderer.prepare(new OversizedItemRenderState(
						renderState,
						0,
						0,
						16,
						16
				), guiRenderState, featureRenderDispatcher, windowScaleFactor);
			}
		}
		batch.clear();
	}

	private record ComparableItemStack(ItemStack itemStack) {
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			return ItemStack.matches(this.itemStack, ((ComparableItemStack) o).itemStack);
		}

		@Override
		public int hashCode() {
			return itemStack.getItem().hashCode();
		}
	}

	private static class ItemGuiElementRenderer extends OversizedItemRenderer {
		private Object modelIdentity;

		@Override
		public void invalidateTexture() {
			this.modelIdentity = null;
		}

		@Override
		protected void renderToTexture(OversizedItemRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
			poseStack.scale(1f, -1f, -1f);

			var guiItemRenderState = renderState.guiItemRenderState();
			var itemRenderState = guiItemRenderState.itemStackRenderState();
			var gameRenderer = Minecraft.getInstance().gameRenderer;

			gameRenderer.lighting().setupFor(
					itemRenderState.usesBlockLight()
							? Lighting.Entry.ITEMS_3D
							: Lighting.Entry.ITEMS_FLAT
			);

			itemRenderState.submit(
					poseStack,
					submitNodeCollector,
					0xF000F0,
					OverlayTexture.NO_OVERLAY,
					0
			);

			this.modelIdentity = itemRenderState.getModelIdentity();
		}

		@Override
		public boolean textureIsReadyToBlit(OversizedItemRenderState renderState) {
			var itemRenderState = renderState.guiItemRenderState().itemStackRenderState();
			return !itemRenderState.isAnimated() && itemRenderState.getModelIdentity().equals(this.modelIdentity);
		}
	}

}
