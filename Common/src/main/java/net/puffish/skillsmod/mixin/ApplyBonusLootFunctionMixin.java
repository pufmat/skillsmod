package net.puffish.skillsmod.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.registry.entry.RegistryEntry;
import net.puffish.skillsmod.access.EntityAttributeInstanceAccess;
import net.puffish.skillsmod.server.PlayerAttributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ApplyBonusLootFunction.class)
public abstract class ApplyBonusLootFunctionMixin {

	@Shadow
	@Final
	private RegistryEntry<Enchantment> enchantment;

	@ModifyVariable(method = "process", at = @At("STORE"), ordinal = 0)
	private int modifyVariableAtProcess(int i, ItemStack itemStack, LootContext context) {
		if (enchantment.value() == Enchantments.FORTUNE && context.get(LootContextParameters.THIS_ENTITY) instanceof PlayerEntity player) {
			var attribute = ((EntityAttributeInstanceAccess) player.getAttributeInstance(PlayerAttributes.FORTUNE));
			double fortune = attribute.computeIncreasedValueForInitial(i);
			i = (int) fortune;
			if (context.getRandom().nextFloat() < fortune - i) {
				i++;
			}
		}
		return i;
	}
}
