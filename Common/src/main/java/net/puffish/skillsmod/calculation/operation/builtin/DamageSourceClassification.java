package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;

public final class DamageSourceClassification {

	public static void register() {
		BuiltinPrototypes.DAMAGE_SOURCE.registerOperation(
				SkillsMod.createIdentifier("is_magic"),
				BuiltinPrototypes.BOOLEAN,
				OperationFactory.create(DamageSourceClassification::isMagic)
		);
		BuiltinPrototypes.DAMAGE_SOURCE.registerOperation(
				SkillsMod.createIdentifier("is_projectile"),
				BuiltinPrototypes.BOOLEAN,
				OperationFactory.create(DamageSourceClassification::isProjectile)
		);
		BuiltinPrototypes.DAMAGE_SOURCE.registerOperation(
				SkillsMod.createIdentifier("is_melee"),
				BuiltinPrototypes.BOOLEAN,
				OperationFactory.create(DamageSourceClassification::isMelee)
		);
	}

	private static boolean isMagic(DamageSource source) {
		return source.isMagic();
	}

	private static boolean isProjectile(DamageSource source) {
		return source instanceof ProjectileDamageSource;
	}

	private static boolean isMelee(DamageSource source) {
		return source instanceof EntityDamageSource && !isProjectile(source);
	}
}
