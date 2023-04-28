package net.puffish.skillsmod.access;

import net.minecraft.item.ItemStack;

import java.util.Optional;

public interface DamageSourceAccess {
	Optional<ItemStack> getWeapon();
}
