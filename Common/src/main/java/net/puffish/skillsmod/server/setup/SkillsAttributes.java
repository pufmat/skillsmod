package net.puffish.skillsmod.server.setup;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;

public class SkillsAttributes {
	public static final Identifier STAMINA_ID = SkillsMod.createAttribute("player", "stamina");
	public static final EntityAttribute STAMINA = create(
			STAMINA_ID,
			4.0,
			0.0,
			1024.0
	).setTracked(true);

	public static final Identifier MELEE_DAMAGE_ID = SkillsMod.createAttribute("player", "melee_damage");
	public static final EntityAttribute MELEE_DAMAGE = create(
			MELEE_DAMAGE_ID,
			0.0,
			0.0,
			Double.MAX_VALUE
	);

	public static final Identifier RANGED_DAMAGE_ID = SkillsMod.createAttribute("player", "ranged_damage");
	public static final EntityAttribute RANGED_DAMAGE = create(
			RANGED_DAMAGE_ID,
			0.0,
			0.0,
			Double.MAX_VALUE
	);

	public static final Identifier FORTUNE_ID = SkillsMod.createAttribute("player", "fortune");
	public static final EntityAttribute FORTUNE = create(
			FORTUNE_ID,
			0.0,
			0.0,
			Double.MAX_VALUE
	);

	public static final Identifier HEALING_ID = SkillsMod.createAttribute("player", "healing");
	public static final EntityAttribute HEALING = create(
			HEALING_ID,
			0.0,
			0.0,
			Double.MAX_VALUE
	).setTracked(true);

	public static final Identifier JUMP_ID = SkillsMod.createAttribute("player", "jump");
	public static final EntityAttribute JUMP = create(
			JUMP_ID,
			0.0,
			0.0,
			Double.MAX_VALUE
	).setTracked(true);

	public static final Identifier RESISTANCE_ID = SkillsMod.createAttribute("player", "resistance");
	public static final EntityAttribute RESISTANCE = create(
			RESISTANCE_ID,
			0.0,
			0.0,
			Double.MAX_VALUE
	);

	public static final Identifier MINING_SPEED_ID = SkillsMod.createAttribute("player", "mining_speed");
	public static final EntityAttribute MINING_SPEED = create(
			MINING_SPEED_ID,
			0.0,
			0.0,
			Double.MAX_VALUE
	).setTracked(true);

	public static final Identifier SPRINTING_SPEED_ID = SkillsMod.createAttribute("player", "sprinting_speed");
	public static final EntityAttribute SPRINTING_SPEED = create(
			SPRINTING_SPEED_ID,
			0.0,
			0.0,
			Double.MAX_VALUE
	).setTracked(true);

	public static final Identifier KNOCKBACK_ID = SkillsMod.createAttribute("player", "knockback");
	public static final EntityAttribute KNOCKBACK = create(
			KNOCKBACK_ID,
			0.0,
			0.0,
			Double.MAX_VALUE
	).setTracked(true);

	private static EntityAttribute create(Identifier id, double fallback, double min, double max) {
		return new ClampedEntityAttribute(
				id.toTranslationKey("attribute"),
				fallback,
				min,
				max
		);
	}

	public static void register(ServerRegistrar registrar) {
		registrar.register(Registries.ATTRIBUTE, STAMINA_ID, STAMINA);
		registrar.register(Registries.ATTRIBUTE, MELEE_DAMAGE_ID, MELEE_DAMAGE);
		registrar.register(Registries.ATTRIBUTE, RANGED_DAMAGE_ID, RANGED_DAMAGE);
		registrar.register(Registries.ATTRIBUTE, FORTUNE_ID, FORTUNE);
		registrar.register(Registries.ATTRIBUTE, HEALING_ID, HEALING);
		registrar.register(Registries.ATTRIBUTE, JUMP_ID, JUMP);
		registrar.register(Registries.ATTRIBUTE, RESISTANCE_ID, RESISTANCE);
		registrar.register(Registries.ATTRIBUTE, MINING_SPEED_ID, MINING_SPEED);
		registrar.register(Registries.ATTRIBUTE, SPRINTING_SPEED_ID, SPRINTING_SPEED);
		registrar.register(Registries.ATTRIBUTE, KNOCKBACK_ID, KNOCKBACK);
	}
}
