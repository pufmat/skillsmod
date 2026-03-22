package net.puffish.skillsmod.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.puffish.skillsmod.access.GuiGraphicsAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements GuiGraphicsAccess {
	@Shadow
	@Final
	private GuiRenderState guiRenderState;

	@Override
	@Unique
	public GuiRenderState getState() {
		return guiRenderState;
	}
}
