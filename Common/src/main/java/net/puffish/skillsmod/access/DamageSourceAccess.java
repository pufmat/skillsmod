package net.puffish.skillsmod.access;

import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface DamageSourceAccess {
	Optional<ItemStack> puffish_skills$getWeapon();
}
