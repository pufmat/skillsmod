package net.puffish.skillsmod.main;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.event.ClientEventListener;
import net.puffish.skillsmod.client.event.ClientEventReceiver;
import net.puffish.skillsmod.client.keybinding.KeyBindingHandler;
import net.puffish.skillsmod.client.keybinding.KeyBindingReceiver;
import net.puffish.skillsmod.client.network.ClientPacketHandler;
import net.puffish.skillsmod.client.network.ClientPacketSender;
import net.puffish.skillsmod.client.setup.ClientRegistrar;
import net.puffish.skillsmod.network.InPacket;
import net.puffish.skillsmod.network.OutPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FabricClientMain implements ClientModInitializer {
	private final Map<Identifier, CustomPacketPayload.Type<FabricMain.InOutPayload<?>>> outPackets = new HashMap<>();

	@Override
	public void onInitializeClient() {
		SkillsClientMod.setup(
				new ClientRegistrarImpl(),
				new ClientEventReceiverImpl(),
				new KeyBindingReceiverImpl(),
				new ClientPacketSenderImpl()
		);
	}

	private class ClientRegistrarImpl implements ClientRegistrar {
		@Override
		public <T extends InPacket> void registerInPacket(Identifier id, Function<RegistryFriendlyByteBuf, T> reader, ClientPacketHandler<T> handler) {
			var pId = new CustomPacketPayload.Type<FabricMain.InOutPayload<T>>(id);
			PayloadTypeRegistry.clientboundPlay().register(pId, CustomPacketPayload.codec(
					(value, buf) -> value.outPacket().write(buf),
					buf -> new FabricMain.InOutPayload<>(pId, reader.apply(buf), null)
			));
			ClientPlayNetworking.registerGlobalReceiver(
					pId,
					(payload, context) -> handler.handle(payload.inValue())
			);
		}

		@Override
		public void registerOutPacket(Identifier id) {
			outPackets.put(id, new CustomPacketPayload.Type<>(id));
		}
	}

	private static class ClientEventReceiverImpl implements ClientEventReceiver {
		@Override
		public void registerListener(ClientEventListener eventListener) {
			ClientPlayConnectionEvents.JOIN.register(
					(handler, sender, client) -> eventListener.onPlayerJoin()
			);
		}
	}

	private static class KeyBindingReceiverImpl implements KeyBindingReceiver {
		@Override
		public void registerKeyBinding(KeyMapping keyBinding, KeyBindingHandler handler) {
			ClientTickEvents.END_CLIENT_TICK.register(
					client -> {
						if (keyBinding.consumeClick()) {
							handler.handle();
						}
					}
			);
			KeyMappingHelper.registerKeyMapping(keyBinding);
		}
	}

	private class ClientPacketSenderImpl implements ClientPacketSender {
		@Override
		public void send(OutPacket packet) {
			ClientPlayNetworking.send(new FabricMain.InOutPayload<>(outPackets.get(packet.getId()), null, packet));
		}
	}
}
