package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.puffish.skillsmod.access.ImmediateAccess;
import net.puffish.skillsmod.access.MinecraftClientAccess;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ItemBatchedRenderer {

	private final Map<ComparableItemStack, List<ItemEmit>> batch = new HashMap<>();

	private record ItemEmit(
			Matrix4f matrix,
			int x, int y
	) { }

	public void emitItem(DrawContext context, ItemStack item, int x, int y) {
		var emits = batch.computeIfAbsent(
				new ComparableItemStack(item),
				key -> new ArrayList<>()
		);

		emits.add(new ItemEmit(
				context.getMatrices().peek().getPositionMatrix(),
				x, y
		));
	}

	public void draw() {
		for (var entry : batch.entrySet()) {
			var itemStack = entry.getKey().itemStack;

			var client = MinecraftClient.getInstance();

			var bakedModel = client.getItemRenderer().getModel(
					itemStack,
					client.world,
					client.player,
					0
			);

			var matrices = new MatrixStack();
			matrices.translate(0, 0, 150);
			matrices.multiplyPositionMatrix(new Matrix4f().scaling(1f, -1f, 1f));
			matrices.scale(16f, 16f, 16f);

			if (bakedModel.isSideLit()) {
				DiffuseLighting.enableGuiDepthLighting();
			} else {
				DiffuseLighting.disableGuiDepthLighting();
			}

			var clientAccess = (MinecraftClientAccess) client;
			var immediateAccess = (ImmediateAccess) clientAccess.getBufferBuilders().getEntityVertexConsumers();
			var vertexConsumerProvider = new BatchedImmediate(
					immediateAccess.getFallbackBuffer(),
					immediateAccess.getLayerBuffers(),
					entry.getValue()
			);

			client.getItemRenderer().renderItem(
					itemStack,
					ModelTransformationMode.GUI,
					false,
					matrices,
					vertexConsumerProvider,
					0xF000F0,
					OverlayTexture.DEFAULT_UV,
					bakedModel
			);

			vertexConsumerProvider.draw();
		}
		batch.clear();
	}

	private static class BatchedImmediate extends VertexConsumerProvider.Immediate {
		private final List<ItemEmit> emits;

		public BatchedImmediate(BufferBuilder fallbackBuffer, Map<RenderLayer, BufferBuilder> layerBuffers, List<ItemEmit> emits) {
			super(fallbackBuffer, layerBuffers);
			this.emits = emits;
		}

		public void draw(RenderLayer renderLayer) {
			var bufferBuilder = layerBuffers.getOrDefault(renderLayer, fallbackBuffer);
			var sameLayer = Objects.equals(currentLayer, renderLayer.asOptional());

			if (!sameLayer && bufferBuilder == this.fallbackBuffer) {
				return;
			}
			if (!activeConsumers.remove(bufferBuilder)) {
				return;
			}

			bufferBuilder.setSorter(RenderSystem.getVertexSorting());
			var builtBuffer = bufferBuilder.end();

			renderLayer.startDrawing();
			var vertexBuffer = builtBuffer.getParameters().format().getBuffer();
			vertexBuffer.bind();
			vertexBuffer.upload(builtBuffer);

			for (var emit : emits) {
				vertexBuffer.draw(
						new Matrix4f(RenderSystem.getModelViewMatrix())
								.mul(emit.matrix())
								.translate(emit.x(), emit.y(), 0),
						RenderSystem.getProjectionMatrix(),
						RenderSystem.getShader()
				);
			}

			renderLayer.endDrawing();

			if (sameLayer) {
				currentLayer = Optional.empty();
			}
		}
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
}
