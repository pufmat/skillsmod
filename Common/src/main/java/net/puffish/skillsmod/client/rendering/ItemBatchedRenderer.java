package net.puffish.skillsmod.client.rendering;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
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
import java.util.Set;

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
		var matrices = new MatrixStack();
		matrices.translate(0, 0, 150);
		matrices.multiplyPositionMatrix(new Matrix4f().scaling(1f, -1f, 1f));
		matrices.scale(16f, 16f, 16f);

		for (var entry : batch.entrySet()) {
			var itemStack = entry.getKey().itemStack;

			var client = MinecraftClient.getInstance();

			var bakedModel = client.getItemRenderer().getModel(
					itemStack,
					client.world,
					client.player,
					0
			);

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

	private static class BatchedImmediate implements VertexConsumerProvider {
		private final List<ItemEmit> emits;
		private final BufferBuilder fallbackBuffer;
		private final Map<RenderLayer, BufferBuilder> layerBuffers;
		private Optional<RenderLayer> optCurrentLayer = Optional.empty();
		private final Set<BufferBuilder> activeConsumers = Sets.newHashSet();

		private BatchedImmediate(BufferBuilder fallbackBuffer, Map<RenderLayer, BufferBuilder> layerBuffers, List<ItemEmit> emits) {
			this.fallbackBuffer = fallbackBuffer;
			this.layerBuffers = layerBuffers;
			this.emits = emits;
		}

		@Override
		public VertexConsumer getBuffer(RenderLayer layer) {
			var optLayer = layer.asOptional();
			var bufferBuilder = this.getBufferInternal(layer);
			if (!Objects.equals(this.optCurrentLayer, optLayer) || !layer.areVerticesNotShared()) {
				this.optCurrentLayer.ifPresent(currentLayer -> {
					if (!this.layerBuffers.containsKey(currentLayer)) {
						this.draw(currentLayer);
					}
				});
				if (this.activeConsumers.add(bufferBuilder)) {
					bufferBuilder.begin(layer.getDrawMode(), layer.getVertexFormat());
				}
				this.optCurrentLayer = optLayer;
			}
			return bufferBuilder;
		}

		private BufferBuilder getBufferInternal(RenderLayer layer) {
			return this.layerBuffers.getOrDefault(layer, this.fallbackBuffer);
		}

		public void draw() {
			this.optCurrentLayer.ifPresent(layer -> {
				if (this.getBuffer(layer) == this.fallbackBuffer) {
					this.draw(layer);
				}
			});
			for (var layer : this.layerBuffers.keySet()) {
				this.draw(layer);
			}
		}

		private void draw(RenderLayer layer) {
			var bufferBuilder = this.getBufferInternal(layer);
			var same = Objects.equals(this.optCurrentLayer, layer.asOptional());
			if (!same && bufferBuilder == this.fallbackBuffer) {
				return;
			}
			if (!this.activeConsumers.remove(bufferBuilder)) {
				return;
			}

			bufferBuilder.setSorter(RenderSystem.getVertexSorting());
			var builtBuffer = bufferBuilder.end();

			layer.startDrawing();
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
			layer.endDrawing();

			if (same) {
				this.optCurrentLayer = Optional.empty();
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
