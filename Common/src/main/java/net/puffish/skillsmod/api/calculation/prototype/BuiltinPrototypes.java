package net.puffish.skillsmod.api.calculation.prototype;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;

import java.util.Optional;

public final class BuiltinPrototypes {
	private BuiltinPrototypes() { }

	public static final Prototype<Double> NUMBER = Prototype.create(SkillsMod.createIdentifier("number"));
	public static final Prototype<Boolean> BOOLEAN = Prototype.create(SkillsMod.createIdentifier("boolean"));
	public static final Prototype<MinecraftServer> SERVER = Prototype.create(Identifier.of("server"));
	public static final Prototype<ServerWorld> WORLD = Prototype.create(Identifier.of("world"));
	public static final Prototype<EntityType<?>> ENTITY_TYPE = Prototype.create(Identifier.of("entity_type"));
	public static final Prototype<Entity> ENTITY = Prototype.create(Identifier.of("entity"));
	public static final Prototype<LivingEntity> LIVING_ENTITY = Prototype.create(Identifier.of("living_entity"));
	public static final Prototype<ServerPlayerEntity> PLAYER = Prototype.create(Identifier.of("player"));
	public static final Prototype<Item> ITEM = Prototype.create(Identifier.of("item"));
	public static final Prototype<ItemStack> ITEM_STACK = Prototype.create(Identifier.of("item_stack"));
	public static final Prototype<Block> BLOCK = Prototype.create(Identifier.of("block"));
	public static final Prototype<BlockState> BLOCK_STATE = Prototype.create(Identifier.of("block_state"));
	public static final Prototype<DamageType> DAMAGE_TYPE = Prototype.create(Identifier.of("damage_type"));
	public static final Prototype<DamageSource> DAMAGE_SOURCE = Prototype.create(Identifier.of("damage_source"));
	public static final Prototype<StatType<?>> STAT_TYPE = Prototype.create(Identifier.of("stat_type"));
	public static final Prototype<Stat<?>> STAT = Prototype.create(Identifier.of("stat"));
	public static final Prototype<StatusEffectInstance> STATUS_EFFECT_INSTANCE = Prototype.create(Identifier.of("status_effect_instance"));
	public static final Prototype<EntityAttributeInstance> ENTITY_ATTRIBUTE_INSTANCE = Prototype.create(Identifier.of("entity_attribute_instance"));

	static {
		WORLD.registerOperation(
				Identifier.of("get_server"),
				SERVER,
				OperationFactory.create(ServerWorld::getServer)
		);
		WORLD.registerOperation(
				Identifier.of("get_time_of_day"),
				NUMBER,
				OperationFactory.create(world -> (double) world.getTimeOfDay())
		);

		ENTITY.registerOperation(
				Identifier.of("get_type"),
				ENTITY_TYPE,
				OperationFactory.create(Entity::getType)
		);
		ENTITY.registerOperation(
				Identifier.of("get_world"),
				WORLD,
				OperationFactory.create(entity -> (ServerWorld) entity.getWorld())
		);

		LIVING_ENTITY.registerOperation(
				Identifier.of("as_entity"),
				ENTITY,
				OperationFactory.create(p -> p)
		);
		LIVING_ENTITY.registerOperation(
				Identifier.of("get_world"),
				WORLD,
				OperationFactory.create(livingEntity -> (ServerWorld) livingEntity.getWorld())
		);
		LIVING_ENTITY.registerOperation(
				Identifier.of("get_type"),
				ENTITY_TYPE,
				OperationFactory.create(Entity::getType)
		);
		LIVING_ENTITY.registerOperation(
				Identifier.of("get_max_health"),
				NUMBER,
				OperationFactory.create(livingEntity -> (double) livingEntity.getMaxHealth())
		);
		LIVING_ENTITY.registerOperation(
				Identifier.of("get_health"),
				NUMBER,
				OperationFactory.create(livingEntity -> (double) livingEntity.getHealth())
		);

		PLAYER.registerOperation(
				Identifier.of("as_living_entity"),
				LIVING_ENTITY,
				OperationFactory.create(p -> p)
		);
		PLAYER.registerOperation(
				Identifier.of("as_entity"),
				ENTITY,
				OperationFactory.create(p -> p)
		);
		PLAYER.registerOperation(
				Identifier.of("get_world"),
				WORLD,
				OperationFactory.create(ServerPlayerEntity::getWorld)
		);

		ITEM.registerOperation(
				Identifier.of("get_saturation_modifier"),
				NUMBER,
				OperationFactory.create(item -> {
					var fc = item.getComponents().get(DataComponentTypes.FOOD);
					return fc == null ? 0.0 : fc.saturation();
				})
		);
		ITEM.registerOperation(
				Identifier.of("get_nutrition"),
				NUMBER,
				OperationFactory.create(item -> {
					var fc = item.getComponents().get(DataComponentTypes.FOOD);
					return fc == null ? 0.0 : fc.nutrition();
				})
		);

		ITEM_STACK.registerOperation(
				Identifier.of("get_item"),
				ITEM,
				OperationFactory.create(ItemStack::getItem)
		);
		ITEM_STACK.registerOperation(
				Identifier.of("get_count"),
				NUMBER,
				OperationFactory.create(itemStack -> (double) itemStack.getCount())
		);

		BLOCK.registerOperation(
				Identifier.of("get_hardness"),
				NUMBER,
				OperationFactory.create(block -> (double) block.getHardness())
		);
		BLOCK.registerOperation(
				Identifier.of("get_blast_resistance"),
				NUMBER,
				OperationFactory.create(block -> (double) block.getBlastResistance())
		);

		BLOCK_STATE.registerOperation(
				Identifier.of("get_block"),
				BLOCK,
				OperationFactory.create(BlockState::getBlock)
		);

		DAMAGE_SOURCE.registerOperation(
				Identifier.of("get_type"),
				DAMAGE_TYPE,
				OperationFactory.create(DamageSource::getType)
		);
		DAMAGE_SOURCE.registerOperation(
				Identifier.of("get_attacker"),
				ENTITY,
				OperationFactory.createOptional(damageSource -> Optional.ofNullable(damageSource.getAttacker()))
		);
		DAMAGE_SOURCE.registerOperation(
				Identifier.of("get_source"),
				ENTITY,
				OperationFactory.createOptional(damageSource -> Optional.ofNullable(damageSource.getSource()))
		);

		STAT.registerOperation(
				Identifier.of("get_type"),
				STAT_TYPE,
				OperationFactory.create(Stat::getType)
		);

		STATUS_EFFECT_INSTANCE.registerOperation(
				Identifier.of("get_level"),
				NUMBER,
				OperationFactory.create(effect -> (double) (effect.getAmplifier() + 1))
		);
		STATUS_EFFECT_INSTANCE.registerOperation(
				Identifier.of("get_duration"),
				NUMBER,
				OperationFactory.create(effect -> (double) effect.getDuration())
		);

		ENTITY_ATTRIBUTE_INSTANCE.registerOperation(
				Identifier.of("get_value"),
				NUMBER,
				OperationFactory.create(EntityAttributeInstance::getValue)
		);
		ENTITY_ATTRIBUTE_INSTANCE.registerOperation(
				Identifier.of("get_base_value"),
				NUMBER,
				OperationFactory.create(EntityAttributeInstance::getBaseValue)
		);
	}
}
