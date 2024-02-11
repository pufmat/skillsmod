package net.puffish.skillsmod.main;

import com.mojang.brigadier.arguments.ArgumentType;
import io.netty.buffer.Unpooled;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.registration.IDirectionAwarePayloadHandlerBuilder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.mixin.GameRulesAccessor;
import net.puffish.skillsmod.network.InPacket;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.server.event.ServerEventListener;
import net.puffish.skillsmod.server.event.ServerEventReceiver;
import net.puffish.skillsmod.server.network.ServerPacketHandler;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import net.puffish.skillsmod.server.setup.ServerRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Mod(SkillsAPI.MOD_ID)
public class NeoForgeMain {
	private final List<ServerEventListener> serverListeners = new ArrayList<>();
	private final Map<Identifier, PacketBuilder> packetBuilders = new HashMap<>();

	public NeoForgeMain(IEventBus modEventBus, Dist dist) {
		if (dist.isClient()) {
			new NeoForgeClientMain(packetBuilders);
		}

		SkillsMod.setup(
				FMLPaths.CONFIGDIR.get(),
				new ServerRegistrarImpl(modEventBus),
				new ServerEventReceiverImpl(),
				new ServerPacketSenderImpl()
		);

		modEventBus.addListener(this::onRegisterPayloadHandler);

		var neoForgeEventBus = NeoForge.EVENT_BUS;
		neoForgeEventBus.addListener(this::onPlayerLoggedIn);
		neoForgeEventBus.addListener(this::onServerStarting);
		neoForgeEventBus.addListener(this::onOnDatapackSyncEvent);
		neoForgeEventBus.addListener(this::onRegisterCommands);
	}

	private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity serverPlayer) {
			for (var listener : serverListeners) {
				listener.onPlayerJoin(serverPlayer);
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

	public void onRegisterPayloadHandler(RegisterPayloadHandlerEvent event) {
		var registrar = event.registrar(SkillsAPI.MOD_ID);
		for (var entry : packetBuilders.entrySet()) {
			var id = entry.getKey();
			registrar.play(
					id,
					buf -> new SharedCustomPayload(id, new PacketByteBuf(Unpooled.buffer()).writeBytes(buf)),
					entry.getValue()::apply
			);
		}
	}

	private class ServerRegistrarImpl implements ServerRegistrar {
		private final IEventBus modEventBus;

		public ServerRegistrarImpl(IEventBus modEventBus) {
			this.modEventBus = modEventBus;
		}

		@Override
		public <V, T extends V> void register(Registry<V> registry, Identifier id, T entry) {
			var deferredRegister = DeferredRegister.create(registry.getKey(), id.getNamespace());
			deferredRegister.register(id.getPath(), () -> entry);
			deferredRegister.register(modEventBus);
		}

		@Override
		public <T extends GameRules.Rule<T>> void registerGameRule(GameRules.Key<T> key, GameRules.Type<T> type) {
			GameRulesAccessor.getRuleTypes().put(key, type);
		}

		@Override
		public <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void registerArgumentType(Identifier id, Class<A> clazz, ArgumentSerializer<A, T> serializer) {
			var deferredRegister = DeferredRegister.create(RegistryKeys.COMMAND_ARGUMENT_TYPE, id.getNamespace());
			deferredRegister.register(id.getPath(), () -> serializer);
			deferredRegister.register(modEventBus);
			ArgumentTypes.registerByClass(clazz, serializer);
		}

		@Override
		public <T extends InPacket> void registerInPacket(Identifier id, Function<PacketByteBuf, T> reader, ServerPacketHandler<T> handler) {
			packetBuilders.computeIfAbsent(id, key -> new NeoForgeMain.PacketBuilder())
					.setServerHandler(((payload, context) -> {
						var packet = reader.apply(payload.data());
						context.workHandler().execute(() -> handler.handle((ServerPlayerEntity) context.player().orElseThrow(), packet));
					}));
		}

		@Override
		public void registerOutPacket(Identifier id) {
			packetBuilders.computeIfAbsent(id, key -> new NeoForgeMain.PacketBuilder())
					.fallbackClientHandler();
		}
	}

	private class ServerEventReceiverImpl implements ServerEventReceiver {
		@Override
		public void registerListener(ServerEventListener eventListener) {
			serverListeners.add(eventListener);
		}
	}

	private static class ServerPacketSenderImpl implements ServerPacketSender {
		@Override
		public void send(ServerPlayerEntity player, OutPacket packet) {
			player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
					new SharedCustomPayload(packet.getIdentifier(), packet.getBuf())
			));
		}
	}

	public record SharedCustomPayload(Identifier id, PacketByteBuf data) implements CustomPayload {
		@Override
		public void write(PacketByteBuf buf) {
			buf.writeBytes(data.slice());
		}
	}

	public static class PacketBuilder {
		private Optional<IPlayPayloadHandler<SharedCustomPayload>> clientHandler = Optional.empty();
		private Optional<IPlayPayloadHandler<SharedCustomPayload>> serverHandler = Optional.empty();

		private boolean fallbackClient = false;
		private boolean fallbackServer = false;

		public void setClientHandler(IPlayPayloadHandler<SharedCustomPayload> handler) {
			if (clientHandler.isPresent()) {
				throw new IllegalStateException();
			}
			clientHandler = Optional.of(handler);
		}

		public void setServerHandler(IPlayPayloadHandler<SharedCustomPayload> handler) {
			if (serverHandler.isPresent()) {
				throw new IllegalStateException();
			}
			serverHandler = Optional.of(handler);
		}

		public void fallbackClientHandler() {
			fallbackClient = true;
		}

		public void fallbackServerHandler() {
			fallbackServer = true;
		}

		public void apply(IDirectionAwarePayloadHandlerBuilder<SharedCustomPayload, IPlayPayloadHandler<SharedCustomPayload>> handlers) {
			clientHandler.ifPresentOrElse(handlers::client, () -> {
				if (fallbackClient) {
					handlers.client((payload, context) -> { });
				}
			});
			serverHandler.ifPresentOrElse(handlers::server, () -> {
				if (fallbackServer) {
					handlers.server((payload, context) -> { });
				}
			});
		}
	}
}
