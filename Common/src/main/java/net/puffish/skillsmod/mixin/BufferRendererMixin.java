package net.puffish.skillsmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.puffish.skillsmod.access.BuiltBufferAccess;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BufferRenderer.class)
public final class BufferRendererMixin {
	@Inject(
			method = "drawWithGlobalProgramInternal",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private static void injectBeforeDraw(BufferBuilder.BuiltBuffer builtBuffer, CallbackInfo ci, VertexBuffer vertexBuffer) {
		var emits = ((BuiltBufferAccess) builtBuffer).puffish_skills$getEmits();
		if (emits != null) {
			for (var emit : emits) {
				vertexBuffer.draw(
						new Matrix4f(RenderSystem.getModelViewMatrix()).mul(emit),
						RenderSystem.getProjectionMatrix(),
						RenderSystem.getShader()
				);
			}
			ci.cancel();
		}
	}
}
