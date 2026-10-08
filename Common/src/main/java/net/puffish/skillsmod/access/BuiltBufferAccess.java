package net.puffish.skillsmod.access;

import org.joml.Matrix4f;

import java.util.List;

public interface BuiltBufferAccess {
	void puffish_skills$setEmits(List<Matrix4f> emits);

	List<Matrix4f> puffish_skills$getEmits();
}
