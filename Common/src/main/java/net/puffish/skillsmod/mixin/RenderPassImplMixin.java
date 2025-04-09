package net.puffish.skillsmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.RenderPassImpl;
import net.puffish.skillsmod.client.rendering.ItemBatchedRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPassImpl.class)
public final class RenderPassImplMixin {

	@Inject(method = "drawIndexed", at = @At("HEAD"), cancellable = true)
	private void injectAtDrawIndexed(int i, int j, CallbackInfo ci) {
		if (ItemBatchedRenderer.EMITS != null) {
			var emits = ItemBatchedRenderer.EMITS;
			var stack = RenderSystem.getModelViewStack();
			var pass = (RenderPassImpl) (Object) this;

			ItemBatchedRenderer.EMITS = null;

			for (var emit : emits) {
				stack.pushMatrix();
				stack.mul(emit);
				pass.drawIndexed(i, j);
				stack.popMatrix();
			}

			ItemBatchedRenderer.EMITS = emits;

			ci.cancel();
		}
	}

}
