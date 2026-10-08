package net.puffish.skillsmod.mixin;

import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.Matrix4f;
import net.puffish.skillsmod.access.VertexFormatAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(VertexFormat.class)
public class VertexFormatMixin implements VertexFormatAccess {
	@Unique
	private List<Matrix4f> emits;

	@Override
	@Unique
	public void puffish_skills$setEmits(List<Matrix4f> emits) {
		this.emits = emits;
	}

	@Override
	public List<Matrix4f> puffish_skills$getEmits() {
		return emits;
	}
}
