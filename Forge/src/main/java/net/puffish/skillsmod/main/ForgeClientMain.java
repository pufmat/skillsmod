package net.puffish.skillsmod.main;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.UnknownCustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.ChannelBuilder;
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
import java.util.Objects;
import java.util.function.Function;

public class ForgeClientMain {
	private final List<ClientEventListener> clientListeners = new ArrayList<>();
	private final List<KeyBindingWithHandler> keyBindings = new ArrayList<>();

	public ForgeClientMain() {
		var forgeEventBus = MinecraftForge.EVENT_BUS;
		forgeEventBus.addListener(this::onPlayerLoggedIn);
		forgeEventBus.addListener(this::onInputKey);

		SkillsClientMod.setup(
				new ClientRegistrarImpl(),
				new ClientEventReceiverImpl(),
				new KeyBindingReceiverImpl(),
				new ClientPacketSenderImpl()
		);
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

	private record KeyBindingWithHandler(KeyBinding keyBinding, KeyBindingHandler handler) {
	}

	private static class ClientRegistrarImpl implements ClientRegistrar {
		@Override
		public <T extends InPacket> void registerInPacket(Identifier identifier, Function<PacketByteBuf, T> reader, ClientPacketHandler<T> handler) {
			var channel = ChannelBuilder.named(identifier)
					.serverAcceptedVersions((status, version) -> true)
					.clientAcceptedVersions((status, version) -> true)
					.eventNetworkChannel();
			channel.addListener(networkEvent -> {
				var context = networkEvent.getSource();
				if (context.getPacketHandled()) {
					return;
				}
				if (context.isClientSide()) {
					var packet = reader.apply(networkEvent.getPayload());
					context.enqueueWork(() -> handler.handle(packet));
					context.setPacketHandled(true);
				}
			});
		}

		@Override
		public void registerOutPacket(Identifier id) { }
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
					.sendPacket(new CustomPayloadC2SPacket(
							new UnknownCustomPayload(packet.getIdentifier(), packet.getBuf())
					));
		}
	}
}
