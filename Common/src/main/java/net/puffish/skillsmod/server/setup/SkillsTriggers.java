package net.puffish.skillsmod.server.setup;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Optional;

public class SkillsTriggers {

	public static SkillUnlockedTrigger SKILL_UNLOCKED = new SkillUnlockedTrigger();

	public static void register(ServerRegistrar registrar) {
		SkillsAPI.registerSkillUnlockEvent((player, categoryId, skillId) -> {
			SKILL_UNLOCKED.trigger(player, categoryId, skillId);
		});
		registrar.register(BuiltInRegistries.TRIGGER_TYPES, SkillsMod.createIdentifier("skill_unlocked"), SKILL_UNLOCKED);
	}

	public static class SkillUnlockedTrigger extends SimpleCriterionTrigger<SkillUnlockedTrigger.Conditions> {
		public void trigger(ServerPlayer player, Identifier categoryId, String skillId) {
			this.trigger(player, conditions -> conditions.matches(categoryId, skillId));
		}

		public Codec<Conditions> codec() {
			return Conditions.CODEC;
		}

		public record Conditions(
				Optional<ContextAwarePredicate> player,
				Identifier categoryId,
				String skillId
		) implements SimpleCriterionTrigger.SimpleInstance {
			public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
					EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
					Identifier.CODEC.fieldOf("category").forGetter(Conditions::categoryId),
					PrimitiveCodec.STRING.fieldOf("skill").forGetter(Conditions::skillId)
			).apply(instance, Conditions::new));

			public boolean matches(Identifier categoryId, String skillId) {
				return this.categoryId.equals(categoryId) && this.skillId.equals(skillId);
			}
		}
	}
}
