package net.puffish.skillsmod.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
<<<<<<< HEAD
=======
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
>>>>>>> 3f9ad5c (Added play_sound as an optional configuration)
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.client.data.ClientSkillCategoryData;
import net.puffish.skillsmod.client.event.ClientEventListener;
import net.puffish.skillsmod.client.event.ClientEventReceiver;
import net.puffish.skillsmod.client.gui.SimpleToast;
import net.puffish.skillsmod.client.gui.SkillsScreen;
import net.puffish.skillsmod.client.keybinding.KeyBindingReceiver;
import net.puffish.skillsmod.client.network.ClientPacketReceiver;
import net.puffish.skillsmod.client.network.ClientPacketSender;
import net.puffish.skillsmod.client.network.packets.in.ExperienceUpdateInPacket;
import net.puffish.skillsmod.client.network.packets.in.HideCategoryInPacket;
import net.puffish.skillsmod.client.network.packets.in.InvalidConfigInPacket;
import net.puffish.skillsmod.client.network.packets.in.PointsUpdateInPacket;
import net.puffish.skillsmod.client.network.packets.in.ShowCategoryInPacket;
import net.puffish.skillsmod.client.network.packets.in.SkillUnlockInPacket;
import net.puffish.skillsmod.network.Packets;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SkillsClientMod {
	public static final KeyBinding OPEN_KEY_BINDING = new KeyBinding(
			"key.puffish_skills.open",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_K,
			"category.puffish_skills.skills"
	);

	private static SkillsClientMod instance;

	private final Map<Identifier, ClientSkillCategoryData> categories = new LinkedHashMap<>();

	private final ClientPacketSender packetSender;

	private SkillsClientMod(ClientPacketSender packetSender) {
		this.packetSender = packetSender;
	}

	public static SkillsClientMod getInstance() {
		return instance;
	}

	public static void setup(
			ClientEventReceiver eventReceiver,
			KeyBindingReceiver keyBindingReceiver,
			ClientPacketSender packetSender,
			ClientPacketReceiver packetReceiver
	) {
		instance = new SkillsClientMod(packetSender);

		keyBindingReceiver.registerKeyBinding(OPEN_KEY_BINDING, instance::onOpenKeyPress);

		packetReceiver.registerPacket(
				Packets.SHOW_CATEGORY,
				ShowCategoryInPacket::read,
				instance::onShowCategory
		);

		packetReceiver.registerPacket(
				Packets.HIDE_CATEGORY,
				HideCategoryInPacket::read,
				instance::onHideCategory
		);

		packetReceiver.registerPacket(
				Packets.SKILL_UNLOCK_PACKET,
				SkillUnlockInPacket::read,
				instance::onSkillUnlockPacket
		);

		packetReceiver.registerPacket(
				Packets.POINTS_UPDATE_PACKET,
				PointsUpdateInPacket::read,
				instance::onPointsUpdatePacket
		);

		packetReceiver.registerPacket(
				Packets.EXPERIENCE_UPDATE_PACKET,
				ExperienceUpdateInPacket::read,
				instance::onExperienceUpdatePacket
		);

		packetReceiver.registerPacket(
				Packets.INVALID_CONFIG,
				InvalidConfigInPacket::read,
				instance::onInvalidConfig
		);

		eventReceiver.registerListener(instance.new EventListener());
	}

	private void onOpenKeyPress() {
		if (categories.isEmpty()) {
			return;
		}

		MinecraftClient.getInstance().setScreen(new SkillsScreen(
				categories.values().stream().toList()
		));
	}

	private void onShowCategory(ShowCategoryInPacket packet) {
		var category = packet.getCategory();
		categories.put(category.getId(), category);
	}

	private void onHideCategory(HideCategoryInPacket packet) {
		categories.remove(packet.getCategoryId());
	}

	private void onSkillUnlockPacket(SkillUnlockInPacket packet) {
		getCategoryById(packet.getCategoryId()).ifPresent(
				category -> category.unlock(packet.getSkillId())
		);
	}

	private void onExperienceUpdatePacket(ExperienceUpdateInPacket packet) {
		getCategoryById(packet.getCategoryId()).ifPresent(category -> {
			category.setCurrentLevel(packet.getCurrentLevel());
			category.setCurrentExperience(packet.getCurrentExperience());
			category.setRequiredExperience(packet.getRequiredExperience());
		});
	}

	private void onPointsUpdatePacket(PointsUpdateInPacket packet) {
		getCategoryById(packet.getCategoryId()).ifPresent(category -> {
			var oldPointsLeft = category.getPointsLeft();
			category.setSpentPoints(packet.getSpentPoints());
			category.setEarnedPoints(packet.getEarnedPoints());
			var newPointsLeft = category.getPointsLeft();
			var playSound = category.getPlaySound();

			if (packet.announceNewPoints()
					&& newPointsLeft > oldPointsLeft
					&& category.hasAvailableSkill()
			) {
				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
						SkillsMod.createTranslatable(
								"chat",
								"new_point",
								OPEN_KEY_BINDING.getBoundKeyLocalizedText()
						)
				);
<<<<<<< HEAD
=======

				if (!playSound.equals(Identifier.of("", ""))) {
					var soundEvent = SoundEvent.of(playSound);
					MinecraftClient.getInstance().player.playSound(soundEvent, 1.0f, 1.0f);
				}
>>>>>>> 3f9ad5c (Added play_sound as an optional configuration)
			}
		});
	}

	private void onInvalidConfig(InvalidConfigInPacket packet) {
		var client = MinecraftClient.getInstance();
		client.getToastManager().add(SimpleToast.create(
				client,
				Text.literal("Pufferfish's Skills"),
				Text.translatable("toast.puffish_skills.invalid_config.description")
		));
	}

	private Optional<ClientSkillCategoryData> getCategoryById(Identifier categoryId) {
		return Optional.ofNullable(categories.get(categoryId));
	}

	public ClientPacketSender getPacketSender() {
		return packetSender;
	}

	private class EventListener implements ClientEventListener {
		@Override
		public void onPlayerJoin() {
			categories.clear();
		}
	}
}
