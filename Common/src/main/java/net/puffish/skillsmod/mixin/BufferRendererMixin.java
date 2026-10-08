package net.puffish.skillsmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.util.math.Matrix4f;
import net.puffish.skillsmod.access.BuiltBufferAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BufferRenderer.class)
public final class BufferRendererMixin {
	@Inject(
			method = "drawWithShaderInternal",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/Shader;)V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private static void injectBeforeDraw(BufferBuilder.BuiltBuffer builtBuffer, CallbackInfo ci, VertexBuffer vertexBuffer) {
		var emits = ((BuiltBufferAccess) builtBuffer).puffish_skills$getEmits();
		if (emits != null) {
			for (var emit : emits) {
				var matrix = new Matrix4f(RenderSystem.getModelViewMatrix());
				matrix.multiply(emit);
				vertexBuffer.draw(
						matrix,
						RenderSystem.getProjectionMatrix(),
						RenderSystem.getShader()
				);
			}
			ci.cancel();
		}
	}
}
