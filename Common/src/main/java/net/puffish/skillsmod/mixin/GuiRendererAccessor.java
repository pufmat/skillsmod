package net.puffish.skillsmod.mixin;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.OversizedItemRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(GuiRenderer.class)
public interface GuiRendererAccessor {
	@Accessor("oversizedItemRenderers")
	Map<Object, OversizedItemRenderer> getOversizedItemRenderers();

	@Accessor("featureRenderDispatcher")
	FeatureRenderDispatcher getFeatureRenderDispatcher();

	@Invoker("getGuiScaleInvalidatingItemAtlasIfChanged")
	int invokeGetGuiScaleInvalidatingItemAtlasIfChanged();
}
