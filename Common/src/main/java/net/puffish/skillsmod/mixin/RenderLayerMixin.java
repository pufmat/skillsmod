package net.puffish.skillsmod.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Matrix4f;
import net.puffish.skillsmod.access.BufferBuilderAccess;
import net.puffish.skillsmod.access.RenderLayerAccess;
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
					target = "Lnet/minecraft/client/render/BufferRenderer;draw(Lnet/minecraft/client/render/BufferBuilder;)V"
			)
	)
	private BufferBuilder modifyArgAtDrawWithShader(BufferBuilder bufferBuilder) {
		((BufferBuilderAccess) bufferBuilder).puffish_skills$setEmits(emits);
		return bufferBuilder;
	}
}
