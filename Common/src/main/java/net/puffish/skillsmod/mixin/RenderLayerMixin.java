package net.puffish.skillsmod.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.puffish.skillsmod.access.BuiltBufferAccess;
import net.puffish.skillsmod.access.RenderLayerAccess;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(RenderLayer.class)
public final class RenderLayerMixin implements RenderLayerAccess {
	@Unique
	private List<Matrix4f> emits;

	@Override
	@Unique
	public void puffish_skills$setEmits(List<Matrix4f> emits) {
		this.emits = emits;
	}

	@ModifyArg(
			method = "draw",
			index = 0,
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/BufferRenderer;drawWithGlobalProgram(Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;)V"
			)
	)
	private BufferBuilder.BuiltBuffer modifyArgAtDrawWithGlobalProgram(BufferBuilder.BuiltBuffer builtBuffer) {
		((BuiltBufferAccess) builtBuffer).puffish_skills$setEmits(emits);
		return builtBuffer;
	}
}
