package net.puffish.skillsmod.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.client.data.ClientSkillScreenData;
import net.puffish.skillsmod.client.event.ClientEventListener;
import net.puffish.skillsmod.client.event.ClientEventReceiver;
import net.puffish.skillsmod.client.gui.SimpleToast;
import net.puffish.skillsmod.client.gui.SkillsScreen;
import net.puffish.skillsmod.client.keybinding.KeyBindingReceiver;
import net.puffish.skillsmod.client.network.ClientPacketSender;
import net.puffish.skillsmod.client.network.packets.in.ExperienceUpdateInPacket;
import net.puffish.skillsmod.client.network.packets.in.HideCategoryInPacket;
import net.puffish.skillsmod.client.network.packets.in.NewPointInPacket;
import net.puffish.skillsmod.client.network.packets.in.OpenScreenInPacket;
import net.puffish.skillsmod.client.network.packets.in.PointsUpdateInPacket;
import net.puffish.skillsmod.client.network.packets.in.ShowCategoryInPacket;
import net.puffish.skillsmod.client.network.packets.in.ShowToastInPacket;
import net.puffish.skillsmod.client.network.packets.in.SkillUpdateInPacket;
import net.puffish.skillsmod.client.setup.ClientRegistrar;
import net.puffish.skillsmod.network.Packets;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class SkillsClientMod {
	public static final KeyBinding OPEN_KEY_BINDING = new KeyBinding(
			"key.puffish_skills.open",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_K,
			"category.puffish_skills.skills"
	);

	private static SkillsClientMod instance;

	private final ClientSkillScreenData screenData = new ClientSkillScreenData();

	private final ClientPacketSender packetSender;

	private SkillsClientMod(ClientPacketSender packetSender) {
		this.packetSender = packetSender;
	}

	public static SkillsClientMod getInstance() {
		return instance;
	}

	public static void setup(
			ClientRegistrar registrar,
			ClientEventReceiver eventReceiver,
			KeyBindingReceiver keyBindingReceiver,
			ClientPacketSender packetSender
	) {
		instance = new SkillsClientMod(packetSender);

		keyBindingReceiver.registerKeyBinding(OPEN_KEY_BINDING, instance::onOpenKeyPress);

		registrar.registerInPacket(
				Packets.SHOW_CATEGORY,
				ShowCategoryInPacket::read,
				instance::onShowCategory
		);

		registrar.registerInPacket(
				Packets.HIDE_CATEGORY,
				HideCategoryInPacket::read,
				instance::onHideCategory
		);

		registrar.registerInPacket(
				Packets.SKILL_UPDATE,
				SkillUpdateInPacket::read,
				instance::onSkillUpdatePacket
		);

		registrar.registerInPacket(
				Packets.POINTS_UPDATE,
				PointsUpdateInPacket::read,
				instance::onPointsUpdatePacket
		);

		registrar.registerInPacket(
				Packets.EXPERIENCE_UPDATE,
				ExperienceUpdateInPacket::read,
				instance::onExperienceUpdatePacket
		);

		registrar.registerInPacket(
				Packets.SHOW_TOAST,
				ShowToastInPacket::read,
				instance::onShowToast
		);

		registrar.registerInPacket(
				Packets.OPEN_SCREEN,
				OpenScreenInPacket::read,
				instance::onOpenScreenPacket
		);

		registrar.registerInPacket(
				Packets.NEW_POINT,
				NewPointInPacket::read,
				instance::onNewPointPacket
		);

		registrar.registerOutPacket(Packets.SKILL_CLICK);

		eventReceiver.registerListener(instance.new EventListener());
	}

	private void onOpenKeyPress() {
		if (MinecraftClient.getInstance().currentScreen instanceof SkillsScreen screen) {
			screen.close();
		} else {
			openScreen(Optional.empty());
		}
	}

	private void onShowCategory(ShowCategoryInPacket packet) {
		var category = packet.getCategory();
		screenData.putCategory(category.getConfig().id(), category);
	}

	private void onHideCategory(HideCategoryInPacket packet) {
		screenData.removeCategory(packet.getCategoryId());
	}

	private void onSkillUpdatePacket(SkillUpdateInPacket packet) {
		screenData.getCategory(packet.getCategoryId()).ifPresent(category -> {
			if (packet.isUnlocked()) {
				category.unlock(packet.getSkillId());
			} else {
				category.lock(packet.getSkillId());
			}
		});
	}

	private void onExperienceUpdatePacket(ExperienceUpdateInPacket packet) {
		screenData.getCategory(packet.getCategoryId()).ifPresent(category -> {
			category.setCurrentLevel(packet.getCurrentLevel());
			category.setCurrentExperience(packet.getCurrentExperience());
			category.setRequiredExperience(packet.getRequiredExperience());
		});
	}

	private void onPointsUpdatePacket(PointsUpdateInPacket packet) {
		screenData.getCategory(packet.getCategoryId()).ifPresent(category -> {
			category.updatePoints(
					packet.getSpentPoints(),
					packet.getEarnedPoints()
			);
		});
	}

	private void onNewPointPacket(NewPointInPacket packet) {
		screenData.getCategory(packet.getCategoryId()).ifPresent(category -> {
			if (category.hasAnySkillLeft()) {
				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
						SkillsMod.createTranslatable(
								"chat",
								"new_point",
								OPEN_KEY_BINDING.getBoundKeyLocalizedText()
						)
				);
			}
		});
	}

	private void onOpenScreenPacket(OpenScreenInPacket packet) {
		openScreen(packet.getCategoryId());
	}

	private void onShowToast(ShowToastInPacket packet) {
		var client = MinecraftClient.getInstance();
		client.getToastManager().add(SimpleToast.create(
				client,
				Text.literal("Pufferfish's Skills"),
				SkillsMod.createTranslatable("toast", switch (packet.getToastType()) {
					case INVALID_CONFIG -> "invalid_config";
					case MISSING_CONFIG -> "missing_config";
				} + ".description")
		));
	}

	public void openScreen(Optional<Identifier> categoryId) {
		MinecraftClient.getInstance().setScreen(new SkillsScreen(screenData, categoryId));
	}

	public ClientPacketSender getPacketSender() {
		return packetSender;
	}

	private class EventListener implements ClientEventListener {
		@Override
		public void onPlayerJoin() {
			screenData.clearCategories();
		}
	}
}
