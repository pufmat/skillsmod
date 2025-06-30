package net.puffish.skillsmod.access;

import net.minecraft.client.gui.render.OversizedItemGuiElementRenderer;
import net.minecraft.client.render.VertexConsumerProvider;

import java.util.Map;

public interface GuiRendererAccess {
	Map<Object, OversizedItemGuiElementRenderer> getOversizedItems();

	VertexConsumerProvider.Immediate getVertexConsumers();
}
