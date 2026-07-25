package net.puffish.skillsmod.server.setup;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.mixin.CriteriaInvoker;

public class SkillsCriteria {

	public static SkillUnlockedCriterion SKILL_UNLOCKED = new SkillUnlockedCriterion();

	public static void register() {
		SkillsAPI.registerSkillUnlockEvent((player, categoryId, skillId) -> {
			SKILL_UNLOCKED.trigger(player, categoryId, skillId);
		});
		CriteriaInvoker.register(SKILL_UNLOCKED);
	}

	public static class SkillUnlockedCriterion extends AbstractCriterion<SkillUnlockedCriterion.Conditions> {
		public static final Identifier ID = SkillsMod.createIdentifier("skill_unlocked");

		public Conditions conditionsFromJson(JsonObject json, LootContextPredicate context, AdvancementEntityPredicateDeserializer deserializer) {
			var categoryId = new Identifier(JsonHelper.getString(json, "category"));
			var skillId = JsonHelper.getString(json, "skill");
			return new Conditions(context, categoryId, skillId);
		}

		public void trigger(ServerPlayerEntity player, Identifier categoryId, String skillId) {
			this.trigger(player, conditions -> conditions.matches(categoryId, skillId));
		}

		public Identifier getId() {
			return ID;
		}

		public static class Conditions extends AbstractCriterionConditions {
			private final Identifier categoryId;
			private final String skillId;

			public Conditions(LootContextPredicate context, Identifier categoryId, String skillId) {
				super(SkillUnlockedCriterion.ID, context);
				this.categoryId = categoryId;
				this.skillId = skillId;
			}

			public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
				var json = super.toJson(serializer);
				json.addProperty("category", this.categoryId.toString());
				json.addProperty("skill", this.skillId);
				return json;
			}

			public boolean matches(Identifier categoryId, String skillId) {
				return this.categoryId.equals(categoryId) && this.skillId.equals(skillId);
			}
		}
	}
}
