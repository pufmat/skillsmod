package net.puffish.skillsmod.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.puffish.skillsmod.access.BuiltBufferAccess;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(BufferBuilder.BuiltBuffer.class)
public class BuiltBufferMixin implements BuiltBufferAccess {
	@Unique
	private List<Matrix4f> emits;

	@Override
	@Unique
	public void puffish_skills$setEmits(List<Matrix4f> emits) {
		this.emits = emits;
	}

	@Override
	@Unique
	public List<Matrix4f> puffish_skills$getEmits() {
		return emits;
	}
}
