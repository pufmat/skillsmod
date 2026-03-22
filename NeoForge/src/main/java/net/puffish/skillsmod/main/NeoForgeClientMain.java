package net.puffish.skillsmod.main;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.puffish.skillsmod.api.SkillsAPI;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class NeoForgeClientMain {
	private final List<ClientEventListener> clientListeners = new ArrayList<>();
	private final List<KeyBindingWithHandler> keyBindings = new ArrayList<>();
	private final Map<Identifier, CustomPacketPayload.Type<NeoForgeMain.InOutPayload<?>>> outPackets = new HashMap<>();
	private final List<Consumer<PayloadRegistrar>> payloadRegistrations = new ArrayList<>();

	public NeoForgeClientMain(IEventBus modEventBus) {
		SkillsClientMod.setup(
				new ClientRegistrarImpl(),
				new ClientEventReceiverImpl(),
				new KeyBindingReceiverImpl(),
				new ClientPacketSenderImpl()
		);

		modEventBus.addListener(this::onRegisterPayloadHandler);
		modEventBus.addListener(this::onRegisterKeyMappings);

		var neoForgeEventBus = NeoForge.EVENT_BUS;
		neoForgeEventBus.addListener(this::onPlayerLoggedIn);
		neoForgeEventBus.addListener(this::onInputKey);
	}

	private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		for (var keyBinding : keyBindings) {
			event.register(keyBinding.keyMapping);
		}
	}

	private void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
		for (var listener : clientListeners) {
			listener.onPlayerJoin();
		}
	}

	private void onInputKey(InputEvent.Key event) {
		for (var keyBinding : keyBindings) {
			if (keyBinding.keyMapping.consumeClick()) {
				keyBinding.handler.handle();
			}
		}
	}

	private void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
		var registrar = event.registrar(SkillsAPI.MOD_ID);
		for (var payloadRegistration : payloadRegistrations) {
			payloadRegistration.accept(registrar);
		}
	}

	private record KeyBindingWithHandler(KeyMapping keyMapping, KeyBindingHandler handler) { }

	private class ClientRegistrarImpl implements ClientRegistrar {
		@Override
		public <T extends InPacket> void registerInPacket(Identifier id, Function<RegistryFriendlyByteBuf, T> reader, ClientPacketHandler<T> handler) {
			var pId = new CustomPacketPayload.Type<NeoForgeMain.InOutPayload<T>>(id);
			payloadRegistrations.add(registrar -> registrar.playToClient(pId, CustomPacketPayload.codec(
					(value, buf) -> value.outPacket().write(buf),
					buf -> new NeoForgeMain.InOutPayload<>(pId, reader.apply(buf), null)
			), (payload, context) -> handler.handle(payload.inValue())));
		}
		@Override
		public void registerOutPacket(Identifier id) {
			outPackets.put(id, new CustomPacketPayload.Type<>(id));
		}
	}

	private class ClientEventReceiverImpl implements ClientEventReceiver {
		@Override
		public void registerListener(ClientEventListener eventListener) {
			clientListeners.add(eventListener);
		}
	}

	private class KeyBindingReceiverImpl implements KeyBindingReceiver {
		@Override
		public void registerKeyBinding(KeyMapping keyMapping, KeyBindingHandler handler) {
			keyBindings.add(new KeyBindingWithHandler(keyMapping, handler));
		}
	}

	private class ClientPacketSenderImpl implements ClientPacketSender {
		@Override
		public void send(OutPacket packet) {
			Objects.requireNonNull(Minecraft.getInstance().getConnection())
					.send(new ServerboundCustomPayloadPacket(
							new NeoForgeMain.InOutPayload<>(outPackets.get(packet.getId()), null, packet)
					));
		}
	}
}
