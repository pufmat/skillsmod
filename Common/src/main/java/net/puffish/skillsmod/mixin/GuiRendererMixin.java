package net.puffish.skillsmod.mixin;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.OversizedItemGuiElementRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
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
	private Map<Object, OversizedItemGuiElementRenderer> oversizedItems;

	@Shadow
	@Final
	private VertexConsumerProvider.Immediate vertexConsumers;

	@Override
	@Unique
	public Map<Object, OversizedItemGuiElementRenderer> getOversizedItems() {
		return oversizedItems;
	}

	@Override
	@Unique
	public VertexConsumerProvider.Immediate getVertexConsumers() {
		return vertexConsumers;
	}
}
