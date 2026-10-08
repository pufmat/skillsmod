package net.puffish.skillsmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.Matrix4f;
import net.puffish.skillsmod.access.VertexFormatAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(BufferRenderer.class)
public final class BufferRendererMixin {
	@Shadow
	private static void draw(ByteBuffer buffer, VertexFormat.DrawMode drawMode, VertexFormat vertexFormat, int count, VertexFormat.IntType elementFormat, int vertexCount, boolean textured) { }
	@Inject(
			method = "Lnet/minecraft/client/render/BufferRenderer;draw(Ljava/nio/ByteBuffer;Lnet/minecraft/client/render/VertexFormat$DrawMode;Lnet/minecraft/client/render/VertexFormat;ILnet/minecraft/client/render/VertexFormat$IntType;IZ)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void injectAtDraw(ByteBuffer buffer, VertexFormat.DrawMode drawMode, VertexFormat vertexFormat, int count, VertexFormat.IntType elementFormat, int vertexCount, boolean textured, CallbackInfo ci) {
		var access = ((VertexFormatAccess) vertexFormat);
		var emits = access.puffish_skills$getEmits();
		if (emits != null) {
			access.puffish_skills$setEmits(null);
			var original = new Matrix4f(RenderSystem.getModelViewMatrix());
			for (var emit : emits) {
				var matrix = new Matrix4f(original);
				matrix.multiply(emit);
				RenderSystem.getModelViewMatrix().load(matrix);
				draw(buffer, drawMode, vertexFormat, count, elementFormat, vertexCount, textured);
			}
			access.puffish_skills$setEmits(emits);
			RenderSystem.getModelViewMatrix().load(original);
			ci.cancel();
		}
	}
}
