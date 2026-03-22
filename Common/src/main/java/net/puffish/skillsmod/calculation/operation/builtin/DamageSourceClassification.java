package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
		return source.is(DamageTypes.MAGIC)
				|| source.is(DamageTypes.INDIRECT_MAGIC)
				|| source.is(TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("c", "is_magic")))
				|| source.is(TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("neoforge", "is_magic")));
	}

	private static boolean isProjectile(DamageSource source) {
		return (source.getEntity() != null)
				&& (!source.isDirect() || source.is(DamageTypeTags.IS_PROJECTILE));
	}

	private static boolean isMelee(DamageSource source) {
		return (source.getEntity() != null)
				&& source.isDirect()
				&& !source.is(DamageTypeTags.IS_PROJECTILE);
	}
}
