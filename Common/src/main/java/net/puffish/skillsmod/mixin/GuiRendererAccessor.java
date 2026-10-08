package net.puffish.skillsmod.mixin;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.OversizedItemGuiElementRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(GuiRenderer.class)
public interface GuiRendererAccessor {
	@Accessor("oversizedItems")
	Map<Object, OversizedItemGuiElementRenderer> puffish_skills$getOversizedItems();

	@Accessor("vertexConsumers")
	VertexConsumerProvider.Immediate puffish_skills$getVertexConsumers();

	@Invoker("getWindowScaleFactor")
	int puffish_skills$invokeGetWindowScaleFactor();
}
