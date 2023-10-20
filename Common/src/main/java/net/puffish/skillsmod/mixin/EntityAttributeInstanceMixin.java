package net.puffish.skillsmod.mixin;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.puffish.skillsmod.access.EntityAttributeInstanceAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;

@Mixin(EntityAttributeInstance.class)
public abstract class EntityAttributeInstanceMixin implements EntityAttributeInstanceAccess {
	@Shadow
	@Final
	private EntityAttribute type;

	@Shadow
	protected abstract double getBaseValue();

	@Shadow
	protected abstract Collection<EntityAttributeModifier> getModifiersByOperation(EntityAttributeModifier.Operation operation);

	@Override
	@Unique
	public double computeIncreasedValueForInitial(double initial) {
		double value1 = initial + this.getBaseValue();
		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADDITION)) {
			value1 += modifier.getValue();
		}
		double value2 = value1;
		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_BASE)) {
			value2 += value1 * modifier.getValue();
		}
		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_TOTAL)) {
			value2 *= 1.0 + modifier.getValue();
		}
		return this.type.clamp(value2);
	}

	@Override
	@Unique
	public double computeDecreasedValueForInitial(double initial) {
		double value1 = initial - this.getBaseValue();
		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADDITION)) {
			value1 -= modifier.getValue();
		}
		double value2 = value1;
		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_BASE)) {
			value2 -= value1 * modifier.getValue();
		}
		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_TOTAL)) {
			value2 *= 1.0 - modifier.getValue();
		}
		return this.type.clamp(value2);
	}
}
