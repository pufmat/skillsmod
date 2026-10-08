package net.puffish.skillsmod.access;

import net.minecraft.util.math.Matrix4f;

import java.util.List;

public interface VertexFormatAccess {
	void puffish_skills$setEmits(List<Matrix4f> emits);

	List<Matrix4f> puffish_skills$getEmits();
}
