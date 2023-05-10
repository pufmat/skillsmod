package net.puffish.skillsmod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class SimpleToast implements Toast {
	private final SystemToast toast;

	private SimpleToast(SystemToast toast) {
		this.toast = toast;
	}

	public static SimpleToast create(MinecraftClient client, Text title, Text description) {
		return new SimpleToast(SystemToast.create(client, SystemToast.Type.PACK_LOAD_FAILURE, title, description));
	}

	@Override
	public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
		return toast.draw(matrices, manager, startTime);
	}

	@Override
	public int getWidth() {
		return toast.getWidth();
	}

	@Override
	public int getHeight() {
		return toast.getHeight();
	}

	@Override
	public int getRequiredSpaceCount() {
		return toast.getRequiredSpaceCount();
	}
}
