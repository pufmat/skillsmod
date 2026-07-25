package net.puffish.skillsmod.server.setup;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Optional;

public class SkillsCriteria {

	public static SkillUnlockedCriterion SKILL_UNLOCKED = new SkillUnlockedCriterion();

	public static void register(ServerRegistrar registrar) {
		SkillsAPI.registerSkillUnlockEvent((player, categoryId, skillId) -> {
			SKILL_UNLOCKED.trigger(player, categoryId, skillId);
		});
		registrar.register(Registries.CRITERION, SkillsMod.createIdentifier("skill_unlocked"), SKILL_UNLOCKED);
	}

	public static class SkillUnlockedCriterion extends AbstractCriterion<SkillUnlockedCriterion.Conditions> {
		public void trigger(ServerPlayerEntity player, Identifier categoryId, String skillId) {
			this.trigger(player, conditions -> conditions.matches(categoryId, skillId));
		}

		public Codec<Conditions> getConditionsCodec() {
			return Conditions.CODEC;
		}

		public record Conditions(
				Optional<LootContextPredicate> player,
				Identifier categoryId,
				String skillId
		) implements AbstractCriterion.Conditions {
			public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
					EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
					Identifier.CODEC.fieldOf("category").forGetter(Conditions::categoryId),
					PrimitiveCodec.STRING.fieldOf("skill").forGetter(Conditions::skillId)
			).apply(instance, Conditions::new));

			public boolean matches(Identifier categoryId, String skillId) {
				return this.categoryId.equals(categoryId) && this.skillId.equals(skillId);
			}
		}
	}
}
