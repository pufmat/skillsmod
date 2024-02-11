package net.puffish.skillsmod.main;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
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
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class NeoForgeClientMain {
	private final List<ClientEventListener> clientListeners = new ArrayList<>();
	private final List<KeyBindingWithHandler> keyBindings = new ArrayList<>();

	public NeoForgeClientMain(Map<Identifier, NeoForgeMain.PacketBuilder> packetBuilders) {
		SkillsClientMod.setup(
				new ClientRegistrarImpl(packetBuilders),
				new ClientEventReceiverImpl(),
				new KeyBindingReceiverImpl(),
				new ClientPacketSenderImpl()
		);

		var neoForgeEventBus = NeoForge.EVENT_BUS;
		neoForgeEventBus.addListener(this::onPlayerLoggedIn);
		neoForgeEventBus.addListener(this::onInputKey);
	}

	private void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
		for (var listener : clientListeners) {
			listener.onPlayerJoin();
		}
	}

	private void onInputKey(InputEvent.Key event) {
		for (var keyBinding : keyBindings) {
			if (keyBinding.keyBinding.wasPressed()) {
				keyBinding.handler.handle();
			}
		}
	}

	private record KeyBindingWithHandler(KeyBinding keyBinding, KeyBindingHandler handler) { }

	private static class ClientRegistrarImpl implements ClientRegistrar {

		private final Map<Identifier, NeoForgeMain.PacketBuilder> packetBuilders;

		private ClientRegistrarImpl(Map<Identifier, NeoForgeMain.PacketBuilder> packetBuilders) {
			this.packetBuilders = packetBuilders;
		}

		@Override
		public <T extends InPacket> void registerInPacket(Identifier id, Function<PacketByteBuf, T> reader, ClientPacketHandler<T> handler) {
			packetBuilders.computeIfAbsent(id, key -> new NeoForgeMain.PacketBuilder())
					.setClientHandler((payload, context) -> {
						var packet = reader.apply(payload.data());
						context.workHandler().execute(() -> handler.handle(packet));
					});
		}

		@Override
		public void registerOutPacket(Identifier id) {
			packetBuilders.computeIfAbsent(id, key -> new NeoForgeMain.PacketBuilder())
					.fallbackServerHandler();
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
		public void registerKeyBinding(KeyBinding keyBinding, KeyBindingHandler handler) {
			keyBindings.add(new KeyBindingWithHandler(keyBinding, handler));
			var options = MinecraftClient.getInstance().options;
			options.allKeys = ArrayUtils.add(options.allKeys, keyBinding);
		}
	}

	private static class ClientPacketSenderImpl implements ClientPacketSender {
		@Override
		public void send(OutPacket packet) {
			Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler())
					.sendPacket(new CustomPayloadC2SPacket(new NeoForgeMain.SharedCustomPayload(
							packet.getIdentifier(), packet.getBuf()
					)));
		}
	}
}
