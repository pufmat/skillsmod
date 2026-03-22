package net.puffish.skillsmod.access;

import java.util.Map;
import net.minecraft.client.gui.render.pip.OversizedItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

public interface GuiRendererAccess {
	Map<Object, OversizedItemRenderer> getOversizedItems();

	MultiBufferSource.BufferSource getVertexConsumers();
}
