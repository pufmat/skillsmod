package net.puffish.skillsmod.mixin;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.OversizedItemRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.puffish.skillsmod.access.GuiRendererAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(GuiRenderer.class)
public final class GuiRendererMixin implements GuiRendererAccess {
	@Shadow
	@Final
	private Map<Object, OversizedItemRenderer> oversizedItemRenderers;

	@Shadow
	@Final
	private FeatureRenderDispatcher featureRenderDispatcher;

	@Override
	@Unique
	public Map<Object, OversizedItemRenderer> getOversizedItemRenderers() {
		return oversizedItemRenderers;
	}

	@Override
	@Unique
	public FeatureRenderDispatcher getFeatureRenderDispatcher() {
		return featureRenderDispatcher;
	}
}
