package net.puffish.skillsmod.main;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.experience.source.builtin.BreakBlockExperienceSource;
import net.puffish.skillsmod.experience.source.builtin.MineBlockExperienceSource;
import net.puffish.skillsmod.mixin.GameRulesInvoker;
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
	private final Map<Identifier, CustomPayload.Id<InOutPayload<?>>> outPackets = new HashMap<>();
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
		neoForgeEventBus.addListener(this::onBlockBreak);
		neoForgeEventBus.addListener(this::onPlayerLoggedIn);
		neoForgeEventBus.addListener(this::onPlayerLoggedOut);
		neoForgeEventBus.addListener(this::onServerStarting);
		neoForgeEventBus.addListener(this::onOnDatapackSyncEvent);
		neoForgeEventBus.addListener(this::onRegisterCommands);
	}

	private void onBlockBreak(BlockEvent.BreakEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity serverPlayer) {
			var state = event.getState();
			var stack = serverPlayer.getMainHandStack();
			SkillsAPI.updateExperienceSources(
					serverPlayer,
					BreakBlockExperienceSource.class,
					experienceSource -> (int) Math.round(experienceSource.calculation().evaluate(
							new BreakBlockExperienceSource.Data(serverPlayer, state, stack)
					))
			);
			if (EventHooks.doPlayerHarvestCheck(serverPlayer, state, event.getLevel(), event.getPos())) {
				SkillsAPI.updateExperienceSources(
						serverPlayer,
						MineBlockExperienceSource.class,
						es -> (int) Math.round(es.calculation().evaluate(
								new MineBlockExperienceSource.Data(serverPlayer, state, stack)
						))
				);
			}
		}
	}

	private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity serverPlayer) {
			for (var listener : serverListeners) {
				listener.onPlayerJoin(serverPlayer);
			}
		}
	}

	private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity serverPlayer) {
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
			var deferredRegister = DeferredRegister.create(registry.getKey(), id.getNamespace());
			deferredRegister.register(id.getPath(), () -> entry);
			deferredRegister.register(modEventBus);
		}

		@Override
		public <T extends GameRules.Rule<T>> GameRules.Key<T> registerGameRule(String name, GameRules.Category category, GameRules.Type<T> type) {
			return GameRulesInvoker.invokeRegister(name, category, type);
		}

		@Override
		public <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void registerArgumentType(Identifier id, Class<A> clazz, ArgumentSerializer<A, T> serializer) {
			var deferredRegister = DeferredRegister.create(RegistryKeys.COMMAND_ARGUMENT_TYPE, id.getNamespace());
			deferredRegister.register(id.getPath(), () -> serializer);
			deferredRegister.register(modEventBus);
			ArgumentTypes.registerByClass(clazz, serializer);
		}

		@Override
		public <T extends InPacket> void registerInPacket(Identifier id, Function<RegistryByteBuf, T> reader, ServerPacketHandler<T> handler) {
			var pId = new CustomPayload.Id<InOutPayload<T>>(id);
			payloadRegistrations.add(registrar -> registrar.playToServer(pId, CustomPayload.codecOf(
					(value, buf) -> value.outPacket.write(buf),
					buf -> new InOutPayload<>(pId, reader.apply(buf), null)
			), (payload, context) -> handler.handle((ServerPlayerEntity) context.player(), payload.inValue())));
		}

		@Override
		public void registerOutPacket(Identifier id) {
			outPackets.put(id, new CustomPayload.Id<>(id));
			if (FMLEnvironment.getDist().isDedicatedServer()) {
				var pId = new CustomPayload.Id<InOutPayload<?>>(id);
				payloadRegistrations.add(registrar -> registrar.playToClient(pId, CustomPayload.codecOf(
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
		public void send(ServerPlayerEntity player, OutPacket packet) {
			player.networkHandler.send(new CustomPayloadS2CPacket(
					new InOutPayload<>(outPackets.get(packet.getId()), null, packet)
			));
		}
	}

	public record InOutPayload<T>(Id<? extends CustomPayload> id, T inValue, OutPacket outPacket) implements CustomPayload {
		@Override
		public Id<? extends CustomPayload> getId() {
			return id;
		}
	}

	private static class ServerPlatformImpl implements ServerPlatform {
		@Override
		public boolean isFakePlayer(ServerPlayerEntity player) {
			return player instanceof FakePlayer;
		}

		@Override
		public boolean isModLoaded(String id) {
			return ModList.get().isLoaded(id);
		}
	}

}
