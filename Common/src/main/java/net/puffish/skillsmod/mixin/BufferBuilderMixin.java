package net.puffish.skillsmod.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Matrix4f;
import net.puffish.skillsmod.access.BufferBuilderAccess;
import net.puffish.skillsmod.access.DrawArrayParametersAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.util.List;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin implements BufferBuilderAccess {
	@Unique
	private List<Matrix4f> emits;

	@Override
	@Unique
	public void puffish_skills$setEmits(List<Matrix4f> emits) {
		this.emits = emits;
	}

	@Inject(
			method = "popData",
			at = @At("RETURN")
	)
	private void popData(CallbackInfoReturnable<Pair<BufferBuilder.DrawArrayParameters, ByteBuffer>> cir) {
		((DrawArrayParametersAccess) (Object) cir.getReturnValue().getFirst()).puffish_skills$setEmits(emits);
	}
}
