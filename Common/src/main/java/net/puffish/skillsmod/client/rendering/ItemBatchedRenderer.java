package net.puffish.skillsmod.client.rendering;

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
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBatchedRenderer {
	private final Map<ComparableItemStack, BatchedVertexConsumers> batch = new HashMap<>();

	public void emitItem(DrawContext context, ItemStack item, int x, int y) {
		batch.computeIfAbsent(
				new ComparableItemStack(item),
				comparable -> emitItemBatched(comparable.itemStack)
		).emit(context.getMatrices().peek().getPositionMatrix(), x, y);
	}

	public void draw() {
		for (var emit : batch.values()) {
			emit.draw();
		}
		batch.clear();
	}

	private BatchedVertexConsumers emitItemBatched(ItemStack item) {
		var client = MinecraftClient.getInstance();

		var bakedModel = client.getItemRenderer().getModel(
				item,
				client.world,
				client.player,
				0
		);

		var matrices = new MatrixStack();
		matrices.translate(0, 0, 150);
		matrices.multiplyPositionMatrix(new Matrix4f().scaling(1f, -1f, 1f));
		matrices.scale(16f, 16f, 16f);

		var vertexConsumerProvider = new BatchedVertexConsumerProvider();

		client.getItemRenderer().renderItem(
				item,
				ModelTransformationMode.GUI,
				false,
				matrices,
				vertexConsumerProvider,
				0xF000F0,
				OverlayTexture.DEFAULT_UV,
				bakedModel
		);

		return vertexConsumerProvider.end(bakedModel.isSideLit());
	}

	private static class BatchedVertexConsumerProvider implements VertexConsumerProvider {
		private final List<RenderLayer> renderLayers = new ArrayList<>();
		private final List<BufferBuilder> bufferBuilders = new ArrayList<>();

		@Override
		public VertexConsumer getBuffer(RenderLayer layer) {
			var builder = new BufferBuilder(256);
			builder.begin(layer.getDrawMode(), layer.getVertexFormat());
			renderLayers.add(layer);
			bufferBuilders.add(builder);
			return builder;
		}

		public BatchedVertexConsumers end(boolean guiDepthLighting) {
			return new BatchedVertexConsumers(
					renderLayers,
					bufferBuilders.stream().map(BufferBuilder::end).toList(),
					guiDepthLighting
			);
		}
	}

	private static class BatchedVertexConsumers {
		private final List<RenderLayer> renderLayers;
		private final List<BufferBuilder.BuiltBuffer> builtBuffers;
		private final boolean guiDepthLighting;
		private final List<Emit> emits = new ArrayList<>();

		private BatchedVertexConsumers(List<RenderLayer> renderLayers, List<BufferBuilder.BuiltBuffer> bufferBuilders, boolean guiDepthLighting) {
			this.renderLayers = renderLayers;
			this.builtBuffers = bufferBuilders;
			this.guiDepthLighting = guiDepthLighting;
		}

		public void emit(Matrix4f matrix, int x, int y) {
			emits.add(new Emit(matrix, x, y));
		}

		public void draw() {
			if (guiDepthLighting) {
				DiffuseLighting.enableGuiDepthLighting();
			} else {
				DiffuseLighting.disableGuiDepthLighting();
			}

			for (var i = 0; i < builtBuffers.size(); i++) {
				var builtBuffer = builtBuffers.get(i);
				var renderLayer = renderLayers.get(i);

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
			}

			emits.clear();
		}

		private record Emit(Matrix4f matrix, int x, int y) {
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
