package net.puffish.skillsmod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;

public class SimpleToast implements Toast {
	private final SystemToast toast;

	private SimpleToast(SystemToast toast) {
		this.toast = toast;
	}

	public static SimpleToast create(Minecraft client, Component title, Component description) {
		return new SimpleToast(SystemToast.multiline(client, SystemToast.SystemToastId.PACK_LOAD_FAILURE, title, description));
	}

	@Override
	public Visibility getWantedVisibility() {
		return toast.getWantedVisibility();
	}

	@Override
	public void update(ToastManager manager, long time) {
		toast.update(manager, time);
	}

	@Override
	public void render(GuiGraphics graphics, Font font, long startTime) {
		toast.render(graphics, font, startTime);
	}

	@Override
	public int width() {
		return toast.width();
	}

	@Override
	public int height() {
		return toast.height();
	}

	@Override
	public int occcupiedSlotCount() {
		return toast.occcupiedSlotCount();
	}
}
