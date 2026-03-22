package net.puffish.skillsmod.access;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public interface DamageSourceAccess {
	Optional<ItemStack> getWeapon();
}
