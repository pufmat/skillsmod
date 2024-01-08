package net.puffish.skillsmod.access;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;

import java.util.Map;

public interface ImmediateAccess {
	BufferBuilder getFallbackBuffer();
	Map<RenderLayer, BufferBuilder> getLayerBuffers();
}
