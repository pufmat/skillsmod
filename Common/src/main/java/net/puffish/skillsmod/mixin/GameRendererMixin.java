package net.puffish.skillsmod.mixin;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.puffish.skillsmod.access.GameRendererAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements GameRendererAccess {
	@Shadow
	@Final
	private GuiRenderer guiRenderer;

	@Override
	@Unique
	public GuiRenderer getGuiRenderer() {
		return guiRenderer;
	}
}