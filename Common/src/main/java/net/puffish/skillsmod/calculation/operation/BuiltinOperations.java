package net.puffish.skillsmod.calculation.operation;

import net.puffish.skillsmod.calculation.operation.builtin.AttributeOperation;
import net.puffish.skillsmod.calculation.operation.builtin.BlockCondition;
import net.puffish.skillsmod.calculation.operation.builtin.BlockStateCondition;
import net.puffish.skillsmod.calculation.operation.builtin.DamageSourceClassification;
import net.puffish.skillsmod.calculation.operation.builtin.DamageTypeCondition;
import net.puffish.skillsmod.calculation.operation.builtin.EffectOperation;
import net.puffish.skillsmod.calculation.operation.builtin.EntityCondition;
import net.puffish.skillsmod.calculation.operation.builtin.EntityTypeCondition;
import net.puffish.skillsmod.calculation.operation.builtin.ItemCondition;
import net.puffish.skillsmod.calculation.operation.builtin.ItemStackCondition;
import net.puffish.skillsmod.calculation.operation.builtin.ScoreboardOperation;
import net.puffish.skillsmod.calculation.operation.builtin.StatCondition;
import net.puffish.skillsmod.calculation.operation.builtin.StatTypeCondition;
import net.puffish.skillsmod.calculation.operation.builtin.StatValueOperation;
import net.puffish.skillsmod.calculation.operation.builtin.SwitchOperation;
import net.puffish.skillsmod.calculation.operation.builtin.TagCondition;
import net.puffish.skillsmod.calculation.operation.builtin.WorldCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyBlockTagCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyDamageTypeTagCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyEntityTypeTagCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyItemTagCondition;

public class BuiltinOperations {
	public static void register() {
		AttributeOperation.register();
		BlockCondition.register();
		BlockStateCondition.register();
		DamageSourceClassification.register();
		DamageTypeCondition.register();
		EffectOperation.register();
		EntityCondition.register();
		EntityTypeCondition.register();
		ItemCondition.register();
		ItemStackCondition.register();
		ScoreboardOperation.register();
		StatCondition.register();
		StatTypeCondition.register();
		StatValueOperation.register();
		SwitchOperation.register();
		TagCondition.register();
		WorldCondition.register();

		LegacyBlockTagCondition.register();
		LegacyDamageTypeTagCondition.register();
		LegacyEntityTypeTagCondition.register();
		LegacyItemTagCondition.register();
	}
}
