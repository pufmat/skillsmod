package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
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
		return source.isOf(DamageTypes.MAGIC)
				|| source.isOf(DamageTypes.INDIRECT_MAGIC)
				|| source.isIn(TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("c", "is_magic")))
				|| source.isIn(TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("neoforge", "is_magic")));
	}

	private static boolean isProjectile(DamageSource source) {
		return (source.getAttacker() != null)
				&& (!source.isDirect() || source.isIn(DamageTypeTags.IS_PROJECTILE));
	}

	private static boolean isMelee(DamageSource source) {
		return (source.getAttacker() != null)
				&& source.isDirect()
				&& !source.isIn(DamageTypeTags.IS_PROJECTILE);
	}
}
