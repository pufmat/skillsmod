package net.puffish.skillsmod.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.Matrix4f;
import net.puffish.skillsmod.access.DrawArrayParametersAccess;
import net.puffish.skillsmod.access.VertexFormatAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BufferBuilder.DrawArrayParameters.class)
public class DrawArrayParametersMixin implements DrawArrayParametersAccess {
	@Unique
	private List<Matrix4f> emits;

	@Override
	@Unique
	public void puffish_skills$setEmits(List<Matrix4f> emits) {
		this.emits = emits;
	}

	@Inject(
			method = "getVertexFormat",
			at = @At("RETURN")
	)
	private void getVertexFormat(CallbackInfoReturnable<VertexFormat> cir) {
		((VertexFormatAccess) cir.getReturnValue()).puffish_skills$setEmits(emits);
	}
}
