package net.puffish.skillsmod.main;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.network.InPacket;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.server.event.ServerEventListener;
import net.puffish.skillsmod.server.event.ServerEventReceiver;
import net.puffish.skillsmod.server.setup.ServerGameRules;
import net.puffish.skillsmod.server.network.ServerPacketHandler;
import net.puffish.skillsmod.server.network.ServerPacketReceiver;
import net.puffish.skillsmod.server.network.ServerPacketSender;

import java.util.function.Function;

public class FabricMain implements ModInitializer {
	@Override
	public void onInitialize() {
		SkillsMod.setup(
				FabricLoader.getInstance().getConfigDir(),
				Registry::register,
				new ServerGameRulesImpl(),
				new ServerEventReceiverImpl(),
				new ServerPacketSenderImpl(),
				new ServerPacketReceiverImpl()
		);

	}

	private static class ServerGameRulesImpl implements ServerGameRules {
		@Override
		public <T extends GameRules.Rule<T>> GameRules.Key<T> registerGameRule(String namespace, String name, GameRules.Category category, GameRules.Type<T> type) {
			return GameRuleRegistry.register(namespace + ":" + name, category, type);
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

			CommandRegistrationCallback.EVENT.register(
					(dispatcher, registryAccess, environment) -> eventListener.onCommandsRegister(dispatcher)
			);
		}
	}

	private static class ServerPacketSenderImpl implements ServerPacketSender {
		@Override
		public void send(ServerPlayerEntity player, OutPacket packet) {
			ServerPlayNetworking.send(player, packet.getIdentifier(), packet.getBuf());
		}
	}

	private static class ServerPacketReceiverImpl implements ServerPacketReceiver {
		@Override
		public <T extends InPacket> void registerPacket(Identifier identifier, Function<PacketByteBuf, T> reader, ServerPacketHandler<T> handler) {
			ServerPlayNetworking.registerGlobalReceiver(
					identifier,
					(server, player, handler2, buf, responseSender) -> {
						var packet = reader.apply(buf);
						server.execute(
								() -> handler.handle(player, packet)
						);
					}
			);
		}
	}
}
