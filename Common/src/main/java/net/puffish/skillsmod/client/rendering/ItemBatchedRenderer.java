package net.puffish.skillsmod.client.rendering;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.OversizedItemGuiElementRenderer;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.OversizedItemGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.access.DrawContextAccess;
import net.puffish.skillsmod.access.GameRendererAccess;
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

	public void emitItem(DrawContext context, ItemStack item, int x, int y) {
		var emits = batch.computeIfAbsent(
				new ComparableItemStack(item),
				key -> new ArrayList<>()
		);

		emits.add(new Matrix3x2f(context.getMatrices()).translate(x - 8, y - 8));
	}

	public void draw(DrawContext context, ScreenRect scissorArea) {
		var client = MinecraftClient.getInstance();
		var gameRenderer = client.gameRenderer;
		var gameRendererAccess = (GameRendererAccess) gameRenderer;
		var guiRendererAccess = (GuiRendererAccess) gameRendererAccess.getGuiRenderer();
		var guiRendererInvoker = (GuiRendererInvoker) gameRendererAccess.getGuiRenderer();
		var contextAccess = (DrawContextAccess) context;
		var guiRenderState = contextAccess.getState();
		var windowScaleFactor = guiRendererInvoker.invokeGetWindowScaleFactor();
		var vertexConsumers = guiRendererAccess.getVertexConsumers();

		for (var entry : batch.entrySet()) {
			var itemStack = entry.getKey().itemStack;

			var itemRenderState = new KeyedItemRenderState();
			client.getItemModelManager().clearAndUpdate(
					itemRenderState,
					itemStack,
					ItemDisplayContext.GUI,
					client.world,
					client.player,
					0
			);

			itemRenderState.addModelKey(KEY);

			var renderer = guiRendererAccess.getOversizedItems().computeIfAbsent(
					itemRenderState.getModelKey(),
					object -> new ItemGuiElementRenderer(vertexConsumers)
			);

			for (var matrix : entry.getValue()) {
				var renderState = new ItemGuiElementRenderState(
						itemStack.getItem().getName().toString(),
						matrix,
						itemRenderState,
						0,
						0,
						scissorArea
				);
				var box = itemRenderState.getModelBoundingBox();
				renderer.render(new OversizedItemGuiElementRenderState(
						renderState,
						0,
						0,
						MathHelper.ceil(box.getLengthX() * 16),
						MathHelper.ceil(box.getLengthY() * 16)
				), guiRenderState, windowScaleFactor);
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

			return ItemStack.areEqual(this.itemStack, ((ComparableItemStack) o).itemStack);
		}

		@Override
		public int hashCode() {
			return itemStack.getItem().hashCode();
		}
	}

	private static class ItemGuiElementRenderer extends OversizedItemGuiElementRenderer {
		private Object modelKey;

		public ItemGuiElementRenderer(VertexConsumerProvider.Immediate immediate) {
			super(immediate);
		}

		@Override
		public void clearModel() {
			this.modelKey = null;
		}

		@Override
		protected void render(OversizedItemGuiElementRenderState renderState, MatrixStack matrixStack) {
			matrixStack.scale(1f, -1f, -1f);

			var guiItemRenderState = renderState.guiItemRenderState();
			var itemRenderState = guiItemRenderState.state();
			var gameRenderer = MinecraftClient.getInstance().gameRenderer;
			var renderDispatcher = gameRenderer.getEntityRenderDispatcher();

			gameRenderer.getDiffuseLighting().setShaderLights(
					itemRenderState.isSideLit()
							? DiffuseLighting.Type.ITEMS_3D
							: DiffuseLighting.Type.ITEMS_FLAT
			);

			itemRenderState.render(
					matrixStack,
					renderDispatcher.getQueue(),
					0xF000F0,
					OverlayTexture.DEFAULT_UV,
					0
			);
			renderDispatcher.render();

			this.modelKey = itemRenderState.getModelKey();
		}

		@Override
		public boolean shouldBypassScaling(OversizedItemGuiElementRenderState renderState) {
			var itemRenderState = renderState.guiItemRenderState().state();
			return !itemRenderState.isAnimated() && itemRenderState.getModelKey().equals(this.modelKey);
		}
	}

}
