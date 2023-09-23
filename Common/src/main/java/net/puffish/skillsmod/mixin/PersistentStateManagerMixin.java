package net.puffish.skillsmod.mixin;

import java.io.File;
import java.io.FileInputStream;
import java.io.PushbackInputStream;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PersistentStateManager.class)
public abstract class PersistentStateManagerMixin {

	@Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtHelper;getDataVersion(Lnet/minecraft/nbt/NbtCompound;I)I"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void injectAtReadNbt(
			String id,
			DataFixTypes dataFixTypes,
			int currentSaveVersion,
			CallbackInfoReturnable<NbtCompound> cir,
			File file,
			FileInputStream fileInputStream,
			PushbackInputStream pushbackInputStream,
			NbtCompound nbtCompound
	) {
		if (dataFixTypes == null) {
			cir.setReturnValue(nbtCompound);
		}
	}

}
