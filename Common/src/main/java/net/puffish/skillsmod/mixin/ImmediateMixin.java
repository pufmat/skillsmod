package net.puffish.skillsmod.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.puffish.skillsmod.access.ImmediateAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(VertexConsumerProvider.Immediate.class)
public class ImmediateMixin implements ImmediateAccess {
	@Shadow
	@Final
	private BufferBuilder fallbackBuffer;

	@Shadow
	@Final
	private Map<RenderLayer, BufferBuilder> layerBuffers;

	@Override
	public BufferBuilder getFallbackBuffer() {
		return fallbackBuffer;
	}

	@Override
	public Map<RenderLayer, BufferBuilder> getLayerBuffers() {
		return layerBuffers;
	}
}
