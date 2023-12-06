package net.puffish.skillsmod.main;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.UnknownCustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.registries.DeferredRegister;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.mixin.GameRulesAccessor;
import net.puffish.skillsmod.network.InPacket;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.server.event.ServerEventListener;
import net.puffish.skillsmod.server.event.ServerEventReceiver;
import net.puffish.skillsmod.server.setup.ServerRegistrar;
import net.puffish.skillsmod.server.network.ServerPacketHandler;
import net.puffish.skillsmod.server.network.ServerPacketReceiver;
import net.puffish.skillsmod.server.network.ServerPacketSender;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mod(SkillsAPI.MOD_ID)
public class ForgeMain {
	private final List<ServerEventListener> serverListeners = new ArrayList<>();

	public ForgeMain() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ForgeClientMain::new);

		var forgeEventBus = MinecraftForge.EVENT_BUS;
		forgeEventBus.addListener(this::onPlayerLoggedIn);
		forgeEventBus.addListener(this::onServerStarting);
		forgeEventBus.addListener(this::onOnDatapackSyncEvent);
		forgeEventBus.addListener(this::onRegisterCommands);

		SkillsMod.setup(
				FMLPaths.CONFIGDIR.get(),
				new ServerRegistrarImpl(),
				new ServerEventReceiverImpl(),
				new ServerPacketSenderImpl(),
				new ServerPacketReceiverImpl()
		);
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

	private static class ServerRegistrarImpl implements ServerRegistrar {
		@Override
		public <V, T extends V> void register(Registry<V> registry, Identifier id, T entry) {
			var deferredRegister = DeferredRegister.create(registry.getKey(), id.getNamespace());
			deferredRegister.register(id.getPath(), () -> entry);
			deferredRegister.register(FMLJavaModLoadingContext.get().getModEventBus());
		}

		@Override
		public <T extends GameRules.Rule<T>> void registerGameRule(GameRules.Key<T> key, GameRules.Type<T> type) {
			GameRulesAccessor.getRuleTypes().put(key, type);
		}

		@Override
		public <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void registerArgumentType(Identifier id, Class<A> clazz, ArgumentSerializer<A, T> serializer) {
			ArgumentTypes.registerByClass(clazz, serializer);
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
					new UnknownCustomPayload(
							packet.getIdentifier(),
							packet.getBuf()
					)
			));
		}
	}

	private static class ServerPacketReceiverImpl implements ServerPacketReceiver {
		@Override
		public <T extends InPacket> void registerPacket(Identifier identifier, Function<PacketByteBuf, T> reader, ServerPacketHandler<T> handler) {
			var channel = ChannelBuilder.named(identifier)
					.serverAcceptedVersions((status, version) -> true)
					.clientAcceptedVersions((status, version) -> true)
					.eventNetworkChannel();
			channel.addListener(networkEvent -> {
				var context = networkEvent.getSource();
				if (context.getPacketHandled()) {
					return;
				}
				if (context.isServerSide()) {
					var packet = reader.apply(networkEvent.getPayload());
					context.enqueueWork(() -> handler.handle(context.getSender(), packet));
					context.setPacketHandled(true);
				}
			});
		}
	}
}
