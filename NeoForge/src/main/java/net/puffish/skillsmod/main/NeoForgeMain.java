package net.puffish.skillsmod.main;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.network.InPacket;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.server.event.ServerEventListener;
import net.puffish.skillsmod.server.event.ServerEventReceiver;
import net.puffish.skillsmod.server.network.ServerPacketHandler;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import net.puffish.skillsmod.server.setup.ServerPlatform;
import net.puffish.skillsmod.server.setup.ServerRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Mod(SkillsAPI.MOD_ID)
public class NeoForgeMain {
	private final List<ServerEventListener> serverListeners = new ArrayList<>();
	private final Map<Identifier, CustomPacketPayload.Type<InOutPayload<?>>> outPackets = new HashMap<>();
	private final List<Consumer<PayloadRegistrar>> payloadRegistrations = new ArrayList<>();

	public NeoForgeMain(IEventBus modEventBus, Dist dist) {
		if (dist.isClient()) {
			new NeoForgeClientMain(modEventBus);
		}

		SkillsMod.setup(
				FMLPaths.CONFIGDIR.get(),
				new ServerRegistrarImpl(modEventBus),
				new ServerEventReceiverImpl(),
				new ServerPacketSenderImpl(),
				new ServerPlatformImpl()
		);

		modEventBus.addListener(this::onRegisterPayloadHandler);

		var neoForgeEventBus = NeoForge.EVENT_BUS;
		neoForgeEventBus.addListener(this::onPlayerLoggedIn);
		neoForgeEventBus.addListener(this::onPlayerLoggedOut);
		neoForgeEventBus.addListener(this::onServerStarting);
		neoForgeEventBus.addListener(this::onOnDatapackSyncEvent);
		neoForgeEventBus.addListener(this::onRegisterCommands);
	}

	private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			for (var listener : serverListeners) {
				listener.onPlayerJoin(serverPlayer);
			}
		}
	}

	private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			for (var listener : serverListeners) {
				listener.onPlayerLeave(serverPlayer);
			}
		}
	}

	private void onServerStarting(ServerStartingEvent event) {
		var server = event.getServer();
		for (var listener : serverListeners) {
			listener.onServerStarting(server);
		}
	}

	private void onOnDatapackSyncEvent(OnDatapackSyncEvent event) {
		if (event.getPlayer() != null) {
			return;
		}
		var server = event.getPlayerList().getServer();
		for (var listener : serverListeners) {
			listener.onServerReload(server);
		}
	}

	private void onRegisterCommands(RegisterCommandsEvent event) {
		var dispatcher = event.getDispatcher();
		for (var listener : serverListeners) {
			listener.onCommandsRegister(dispatcher);
		}
	}

	public void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
		var registrar = event.registrar(SkillsAPI.MOD_ID);
		for (var payloadRegistration : payloadRegistrations) {
			payloadRegistration.accept(registrar);
		}
	}

	private class ServerRegistrarImpl implements ServerRegistrar {
		private final IEventBus modEventBus;

		public ServerRegistrarImpl(IEventBus modEventBus) {
			this.modEventBus = modEventBus;
		}

		@Override
		public <V, T extends V> void register(Registry<V> registry, Identifier id, T entry) {
			var deferredRegister = DeferredRegister.create(registry.key(), id.getNamespace());
			deferredRegister.register(id.getPath(), () -> entry);
			deferredRegister.register(modEventBus);
		}

		@Override
		public <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void registerArgumentType(Identifier id, Class<A> clazz, ArgumentTypeInfo<A, T> info) {
			var deferredRegister = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, id.getNamespace());
			deferredRegister.register(id.getPath(), () -> info);
			deferredRegister.register(modEventBus);
			ArgumentTypeInfos.registerByClass(clazz, info);
		}

		@Override
		public <T extends InPacket> void registerInPacket(Identifier id, Function<RegistryFriendlyByteBuf, T> reader, ServerPacketHandler<T> handler) {
			var pId = new CustomPacketPayload.Type<InOutPayload<T>>(id);
			payloadRegistrations.add(registrar -> registrar.playToServer(pId, CustomPacketPayload.codec(
					(value, buf) -> value.outPacket.write(buf),
					buf -> new InOutPayload<>(pId, reader.apply(buf), null)
			), (payload, context) -> handler.handle((ServerPlayer) context.player(), payload.inValue())));
		}

		@Override
		public void registerOutPacket(Identifier id) {
			outPackets.put(id, new CustomPacketPayload.Type<>(id));
			if (FMLEnvironment.getDist().isDedicatedServer()) {
				var pId = new CustomPacketPayload.Type<InOutPayload<?>>(id);
				payloadRegistrations.add(registrar -> registrar.playToClient(pId, CustomPacketPayload.codec(
						(value, buf) -> value.outPacket.write(buf),
						buf -> null
				), (payload, context) -> { }));
			}
		}
	}

	private class ServerEventReceiverImpl implements ServerEventReceiver {
		@Override
		public void registerListener(ServerEventListener eventListener) {
			serverListeners.add(eventListener);
		}
	}

	private class ServerPacketSenderImpl implements ServerPacketSender {
		@Override
		public void send(ServerPlayer player, OutPacket packet) {
			player.connection.send(new ClientboundCustomPayloadPacket(
					new InOutPayload<>(outPackets.get(packet.getId()), null, packet)
			));
		}
	}

	public record InOutPayload<T>(Type<? extends CustomPacketPayload> type, T inValue, OutPacket outPacket) implements CustomPacketPayload {
		@Override
		public Type<? extends CustomPacketPayload> type() {
			return type;
		}
	}

	private static class ServerPlatformImpl implements ServerPlatform {
		@Override
		public boolean isFakePlayer(ServerPlayer player) {
			return player instanceof FakePlayer;
		}

		@Override
		public boolean isModLoaded(String id) {
			return ModList.get().isLoaded(id);
		}
	}

}
