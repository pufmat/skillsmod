package net.puffish.skillsmod.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.puffish.skillsmod.access.GuiGraphicsExtractorAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin implements GuiGraphicsExtractorAccess {
	@Shadow
	@Final
	private GuiRenderState guiRenderState;

	@Override
	@Unique
	public GuiRenderState getGuiRenderState() {
		return guiRenderState;
	}
}
