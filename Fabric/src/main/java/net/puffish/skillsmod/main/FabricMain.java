package net.puffish.skillsmod.main;

import com.mojang.brigadier.arguments.ArgumentType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.mixin.GameRulesAccessor;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.server.event.ServerEventListener;
import net.puffish.skillsmod.server.event.ServerEventReceiver;
import net.puffish.skillsmod.server.network.ServerPacketHandler;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import net.puffish.skillsmod.server.setup.ServerRegistrar;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FabricMain implements ModInitializer {
	private final Map<Identifier, CustomPayload.Id<InOutPayload<?>>> outPackets = new HashMap<>();

	@Override
	public void onInitialize() {
		SkillsMod.setup(
				FabricLoader.getInstance().getConfigDir(),
				new ServerRegistrarImpl(),
				new ServerEventReceiverImpl(),
				new ServerPacketSenderImpl()
		);
	}

	private class ServerRegistrarImpl implements ServerRegistrar {
		@Override
		public <V, T extends V> void register(Registry<V> registry, Identifier id, T entry) {
			Registry.register(registry, id, entry);
		}

		@Override
		public <T extends GameRules.Rule<T>> void registerGameRule(GameRules.Key<T> key, GameRules.Type<T> type) {
			GameRulesAccessor.getRuleTypes().put(key, type);
		}

		@Override
		public <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void registerArgumentType(Identifier id, Class<A> clazz, ArgumentSerializer<A, T> serializer) {
			ArgumentTypeRegistry.registerArgumentType(id, clazz, serializer);
		}

		@Override
		public <T extends net.puffish.skillsmod.network.InPacket> void registerInPacket(Identifier id, Function<RegistryByteBuf, T> reader, ServerPacketHandler<T> handler) {
			var pId = new CustomPayload.Id<InOutPayload<T>>(id);
			PayloadTypeRegistry.playC2S().register(pId, CustomPayload.codecOf(
					(value, buf) -> value.outPacket.write(buf),
					buf -> new InOutPayload<>(pId, reader.apply(buf), null)
			));
			ServerPlayNetworking.registerGlobalReceiver(
					pId,
					(payload, context) -> handler.handle(context.player(), payload.inValue())
			);
		}

		@Override
		public void registerOutPacket(Identifier id) {
			outPackets.put(id, new CustomPayload.Id<>(id));
			if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
				var pId = new CustomPayload.Id<InOutPayload<?>>(id);
				PayloadTypeRegistry.playS2C().register(pId, CustomPayload.codecOf(
						(value, buf) -> value.outPacket.write(buf),
						buf -> null
				));
			}
		}
	}

	private static class ServerEventReceiverImpl implements ServerEventReceiver {
		@Override
		public void registerListener(ServerEventListener eventListener) {
			ServerLifecycleEvents.SERVER_STARTING.register(eventListener::onServerStarting);

			ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
					(server, resourceManager, success) -> eventListener.onServerReload(server)
			);

			ServerPlayConnectionEvents.JOIN.register(
					(handler, sender, server) -> eventListener.onPlayerJoin(handler.player)
			);

			ServerPlayConnectionEvents.DISCONNECT.register(
					(handler, server) -> eventListener.onPlayerLeave(handler.player)
			);

			CommandRegistrationCallback.EVENT.register(
					(dispatcher, registryAccess, environment) -> eventListener.onCommandsRegister(dispatcher)
			);
		}
	}

	private class ServerPacketSenderImpl implements ServerPacketSender {
		@Override
		public void send(ServerPlayerEntity player, OutPacket packet) {
			ServerPlayNetworking.send(player, new InOutPayload<>(outPackets.get(packet.getId()), null, packet));
		}
	}

	public record InOutPayload<T>(Id<? extends CustomPayload> id, T inValue, OutPacket outPacket) implements CustomPayload {
		@Override
		public Id<? extends CustomPayload> getId() {
			return id;
		}
	}
}
