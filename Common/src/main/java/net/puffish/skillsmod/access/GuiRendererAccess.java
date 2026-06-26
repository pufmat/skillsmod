package net.puffish.skillsmod.access;

import net.minecraft.client.gui.render.pip.OversizedItemRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;

import java.util.Map;

public interface GuiRendererAccess {
	Map<Object, OversizedItemRenderer> getOversizedItemRenderers();

	FeatureRenderDispatcher getFeatureRenderDispatcher();
}
