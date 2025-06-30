package net.puffish.skillsmod.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.puffish.skillsmod.access.DrawContextAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DrawContext.class)
public class DrawContextMixin implements DrawContextAccess {
	@Shadow
	@Final
	private GuiRenderState state;

	@Override
	@Unique
	public GuiRenderState getState() {
		return state;
	}
}
