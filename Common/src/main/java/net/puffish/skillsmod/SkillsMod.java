package net.puffish.skillsmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.resource.Resource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.puffish.skillsmod.commands.CategoryCommand;
import net.puffish.skillsmod.commands.ExperienceCommand;
import net.puffish.skillsmod.commands.PointsCommand;
import net.puffish.skillsmod.commands.SkillsCommand;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.config.ModConfig;
import net.puffish.skillsmod.config.PackConfig;
import net.puffish.skillsmod.config.experience.ExperienceSourceConfig;
import net.puffish.skillsmod.config.reader.ConfigReader;
import net.puffish.skillsmod.config.reader.FileConfigReader;
import net.puffish.skillsmod.config.reader.PackConfigReader;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.experience.ExperienceSource;
import net.puffish.skillsmod.experience.builtin.CraftItemExperienceSource;
import net.puffish.skillsmod.experience.builtin.EatFoodExperienceSource;
import net.puffish.skillsmod.experience.builtin.IncreaseStatExperienceSource;
import net.puffish.skillsmod.experience.builtin.KillEntityExperienceSource;
import net.puffish.skillsmod.experience.builtin.MineBlockExperienceSource;
import net.puffish.skillsmod.experience.builtin.TakeDamageExperienceSource;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.rewards.RewardContext;
import net.puffish.skillsmod.rewards.builtin.AttributeReward;
import net.puffish.skillsmod.rewards.builtin.CommandReward;
import net.puffish.skillsmod.rewards.builtin.ScoreboardReward;
import net.puffish.skillsmod.rewards.builtin.TagReward;
import net.puffish.skillsmod.server.PlayerAttributes;
import net.puffish.skillsmod.server.SkillsGameRules;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.server.data.PlayerData;
import net.puffish.skillsmod.server.data.ServerData;
import net.puffish.skillsmod.server.event.ServerEventListener;
import net.puffish.skillsmod.server.event.ServerEventReceiver;
import net.puffish.skillsmod.server.network.ServerPacketReceiver;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import net.puffish.skillsmod.server.network.packets.in.SkillClickInPacket;
import net.puffish.skillsmod.server.network.packets.out.ExperienceUpdateOutPacket;
import net.puffish.skillsmod.server.network.packets.out.HideCategoryOutPacket;
import net.puffish.skillsmod.server.network.packets.out.InvalidConfigOutPacket;
import net.puffish.skillsmod.server.network.packets.out.PointsUpdateOutPacket;
import net.puffish.skillsmod.server.network.packets.out.ShowCategoryOutPacket;
import net.puffish.skillsmod.server.network.packets.out.SkillUnlockOutPacket;
import net.puffish.skillsmod.server.setup.ServerGameRules;
import net.puffish.skillsmod.server.setup.ServerRegistrar;
import net.puffish.skillsmod.utils.ChangeListener;
import net.puffish.skillsmod.utils.PathUtils;
import net.puffish.skillsmod.utils.PrefixedLogger;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkillsMod {
	public static final int CONFIG_VERSION = 1;

	private static SkillsMod instance;

	private final PrefixedLogger logger = new PrefixedLogger(SkillsAPI.MOD_ID);

	private final Path modConfigDir;
	private final ServerPacketSender packetSender;

	private final ChangeListener<Optional<Map<Identifier, CategoryConfig>>> categories = new ChangeListener<>(
			Optional.empty(),
			() -> { }
	);

	private SkillsMod(Path modConfigDir, ServerPacketSender packetSender) {
		this.modConfigDir = modConfigDir;
		this.packetSender = packetSender;
	}

	public static SkillsMod getInstance() {
		return instance;
	}

	public static void setup(
			Path configDir,
			ServerRegistrar registrar,
			ServerGameRules gameRules,
			ServerEventReceiver eventReceiver,
			ServerPacketSender packetSender,
			ServerPacketReceiver packetReceiver
	) {
		Path modConfigDir = configDir.resolve(SkillsAPI.MOD_ID);
		try {
			Files.createDirectories(modConfigDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		instance = new SkillsMod(modConfigDir, packetSender);

		packetReceiver.registerPacket(
				Packets.SKILL_CLICK_PACKET,
				SkillClickInPacket::read,
				instance::onSkillClickPacket
		);

		eventReceiver.registerListener(instance.new EventListener());

		PlayerAttributes.register(registrar);

		SkillsGameRules.register(gameRules);

		AttributeReward.register();
		CommandReward.register();
		ScoreboardReward.register();
		TagReward.register();

		MineBlockExperienceSource.register();
		KillEntityExperienceSource.register();
		EatFoodExperienceSource.register();
		CraftItemExperienceSource.register();
		TakeDamageExperienceSource.register();
		IncreaseStatExperienceSource.register();
	}

	public static Identifier createIdentifier(String path) {
		return new Identifier(SkillsAPI.MOD_ID, path);
	}

	public static MutableText createTranslatable(String type, String path, Object... args) {
		return new TranslatableText(Util.createTranslationKey(type, createIdentifier(path)), args);
	}

	public static Identifier createAttribute(String type, String name) {
		return createIdentifier(type + "." + name);
	}

	public PrefixedLogger getLogger() {
		return logger;
	}

	private void copyConfigFromJar() {
		PathUtils.copyFileFromJar(
				Path.of("config", "config.json"),
				modConfigDir.resolve("config.json")
		);

		var categories = List.of(
				"combat",
				"mining"
		);

		var files = List.of(
				"category.json",
				"definitions.json",
				"skills.json",
				"connections.json",
				"experience.json"
		);

		for (var category : categories) {
			for (var file : files) {
				PathUtils.copyFileFromJar(
						Path.of("config", "categories", category, file),
						modConfigDir.resolve(Path.of("categories", category, file))
				);
			}
		}
	}

	private void loadModConfig(MinecraftServer server) {
		if (!Files.exists(modConfigDir) || PathUtils.isDirectoryEmpty(modConfigDir)) {
			copyConfigFromJar();
		}

		var reader = new FileConfigReader(modConfigDir);
		var cumulatedMap = new LinkedHashMap<Identifier, CategoryConfig>();

		reader.read(Path.of("config.json"))
				.andThen(ModConfig::parse)
				.andThen(modConfig ->
						loadConfig(reader, modConfig, server)
								.mapSuccess(map -> {
									cumulatedMap.putAll(map);
									return modConfig;
								})
				)
				.peek(modConfig -> {
					cumulatedMap.putAll(loadPackConfig(modConfig, server));

					categories.set(Optional.of(cumulatedMap), () -> {
						for (var category : cumulatedMap.values()) {
							category.dispose(server);
						}
					});
				}, failure -> {
					logger.error("Configuration could not be loaded:"
							+ System.lineSeparator()
							+ failure.getMessages().stream().collect(Collectors.joining(System.lineSeparator()))
					);

					categories.set(Optional.empty(), () -> { });
				});
	}

	private Result<Map<Identifier, CategoryConfig>, Failure> loadConfig(ConfigReader reader, ModConfig modConfig, MinecraftServer server) {
		var context = new ConfigContext(server);

		return reader.readCategories(Identifier.DEFAULT_NAMESPACE, modConfig.getCategories(), context)
				.ifSuccess(map -> {
					if (modConfig.getShowWarnings() && !context.warnings().isEmpty()) {
						logger.warn("Configuration loaded successfully with warning(s):"
								+ System.lineSeparator()
								+ ManyFailures.ofList(context.warnings()).getMessages().stream().collect(Collectors.joining(System.lineSeparator()))
						);
					} else {
						logger.info("Configuration loaded successfully!");
					}
				});
	}

	private Map<Identifier, CategoryConfig> loadPackConfig(ModConfig modConfig, MinecraftServer server) {
		var context = new ConfigContext(server);
		var cumulatedMap = new LinkedHashMap<Identifier, CategoryConfig>();

		var resources = context.resourceManager().findResources(
				SkillsAPI.MOD_ID,
				id -> id.endsWith("config.json")
		);

		for (var id : resources) {
			Resource resource;
			try {
				resource = context.resourceManager().getResource(id);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			var name = id.getNamespace();
			var reader = new PackConfigReader(context.resourceManager(), name);

			reader.readResource(id, resource)
					.andThen(rootElement -> PackConfig.parse(name, rootElement))
					.andThen(packConfig -> reader.readCategories(name, packConfig.getCategories(), context))
					.peek(map -> {
						if (modConfig.getShowWarnings() && !context.warnings().isEmpty()) {
							logger.warn("Data pack `" + name + "` loaded successfully with warning(s):"
									+ System.lineSeparator()
									+ ManyFailures.ofList(context.warnings()).getMessages().stream().collect(Collectors.joining(System.lineSeparator()))
							);
						} else {
							logger.info("Data pack `" + name + "` loaded successfully!");
						}
						cumulatedMap.putAll(map);
					}, failure ->
							logger.error("Data pack `" + name + "` could not be loaded:"
									+ System.lineSeparator()
									+ failure.getMessages().stream().collect(Collectors.joining(System.lineSeparator()))
					));
		}

		return cumulatedMap;
	}

	private void onSkillClickPacket(ServerPlayerEntity player, SkillClickInPacket packet) {
		if (player.isSpectator()) {
			return;
		}
		tryUnlockSkill(player, packet.getCategoryId(), packet.getSkillId(), false);
	}

	public void unlockSkill(ServerPlayerEntity player, Identifier categoryId, String skillId) {
		tryUnlockSkill(player, categoryId, skillId, true);
	}

	private void tryUnlockSkill(ServerPlayerEntity player, Identifier categoryId, String skillId, boolean force) {
		getCategory(categoryId).ifPresent(category -> getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
			if (categoryData.tryUnlockSkill(category, player, skillId, force)) {
				packetSender.send(player, SkillUnlockOutPacket.write(categoryId, skillId));
				syncPoints(player, category, categoryData);
			}
		}));
	}

	public void resetSkills(ServerPlayerEntity player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
			categoryData.resetSkills();
			applyRewards(player, category, categoryData);
			syncCategory(player, category, categoryData);
		}));
	}

	public void eraseCategory(ServerPlayerEntity player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var playerData = getPlayerData(player);
			playerData.removeCategoryData(category);

			syncCategory(player, category);
		});
	}

	public void unlockCategory(ServerPlayerEntity player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var playerData = getPlayerData(player);
			playerData.unlockCategory(category);

			syncCategory(player, category);
		});
	}

	public void lockCategory(ServerPlayerEntity player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var playerData = getPlayerData(player);
			playerData.lockCategory(category);

			syncCategory(player, category);
		});
	}

	public void addExperience(ServerPlayerEntity player, Identifier categoryId, int amount) {
		getCategory(categoryId).ifPresent(category -> {
			if (category.getExperience().isEmpty()) {
				return;
			}

			getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
				categoryData.addExperience(amount);

				syncExperience(player, category, categoryData);
				syncPoints(player, category, categoryData);
			});
		});
	}

	public void setExperience(ServerPlayerEntity player, Identifier categoryId, int amount) {
		getCategory(categoryId).ifPresent(category -> {
			if (category.getExperience().isEmpty()) {
				return;
			}

			getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
				categoryData.setEarnedExperience(amount);

				syncExperience(player, category, categoryData);
				syncPoints(player, category, categoryData);
			});
		});
	}

	public Optional<Integer> getExperience(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).flatMap(category -> {
			if (category.getExperience().isEmpty()) {
				return Optional.empty();
			}

			return getCategoryDataIfUnlocked(player, category).map(CategoryData::getEarnedExperience);
		});
	}

	public void addExtraPoints(ServerPlayerEntity player, Identifier categoryId, int count) {
		getCategory(categoryId).ifPresent(category -> getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
			categoryData.addExtraPoints(count);

			syncPoints(player, category, categoryData);
		}));
	}

	public void setExtraPoints(ServerPlayerEntity player, Identifier categoryId, int count) {
		getCategory(categoryId).ifPresent(category -> getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
			categoryData.setExtraPoints(count);

			syncPoints(player, category, categoryData);
		}));
	}

	public Optional<Integer> getExtraPoints(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId)
				.flatMap(category -> getCategoryDataIfUnlocked(player, category)
						.map(CategoryData::getExtraPoints)
				);
	}

	public Optional<Integer> getPointsLeft(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getCategoryData(category);
			return categoryData.getPointsLeft(category);
		});
	}

	public Collection<Identifier> getUnlockedCategories(ServerPlayerEntity player) {
		var playerData = getPlayerData(player);

		return getAllCategories()
				.stream()
				.filter(playerData::isCategoryUnlocked)
				.map(CategoryConfig::getId)
				.toList();
	}

	public Collection<Identifier> getCategories() {
		return getAllCategories()
				.stream()
				.map(CategoryConfig::getId)
				.toList();
	}

	public Optional<Collection<String>> getUnlockedSkills(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getCategoryData(category);
			return categoryData.getUnlockedSkillIds();
		});
	}

	public Optional<Collection<String>> getSkills(Identifier categoryId) {
		return getCategory(categoryId).map(
				category -> category.getSkills()
						.getAll()
						.stream()
						.map(SkillConfig::getId)
						.toList()
		);
	}

	public boolean hasCategory(Identifier categoryId) {
		return getCategory(categoryId).isPresent();
	}

	public boolean hasSkill(Identifier categoryId, String skillId) {
		return getCategory(categoryId)
				.map(category -> category.getSkills().getById(skillId).isPresent())
				.orElse(false);
	}

	private void showCategory(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData) {
		packetSender.send(player, ShowCategoryOutPacket.write(category, categoryData));
	}

	private void hideCategory(ServerPlayerEntity player, CategoryConfig category) {
		packetSender.send(player, HideCategoryOutPacket.write(category.getId()));
	}

	private void syncPoints(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData) {
		packetSender.send(player, PointsUpdateOutPacket.write(
				category.getId(),
				categoryData.getSpentPoints(category),
				categoryData.getEarnedPoints(category),
				player.getWorld().getGameRules().getBoolean(SkillsGameRules.ANNOUNCE_NEW_POINTS)
		));
	}

	private void syncExperience(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData) {
		packetSender.send(player, ExperienceUpdateOutPacket.write(
				category.getId(),
				categoryData.getCurrentLevel(category),
				categoryData.getCurrentExperience(category),
				categoryData.getRequiredExperience(category)
		));
	}

	public void refreshReward(ServerPlayerEntity player, Identifier type) {
		for (CategoryConfig category : getAllCategories()) {
			var categoryData = getPlayerData(player).getCategoryData(category);
			categoryData.refreshReward(category, player, type);
		}
	}

	public void visitExperienceSources(ServerPlayerEntity player, Function<ExperienceSource, Integer> function) {
		for (CategoryConfig category : getAllCategories()) {
			category.getExperience().ifPresent(experience -> getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
				int amount = 0;

				for (ExperienceSourceConfig experienceSource : experience.getExperienceSources()) {
					amount += function.apply(experienceSource.getInstance());
				}

				if (amount != 0) {
					categoryData.addExperience(amount);

					syncExperience(player, category, categoryData);
					syncPoints(player, category, categoryData);
				}
			}));
		}
	}

	private void applyRewards(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData) {
		categoryData.applyRewards(category, player);
	}

	private void resetRewards(ServerPlayerEntity player, CategoryConfig category) {
		for (var definition : category.getDefinitions().getAll()) {
			for (var reward : definition.getRewards()) {
				reward.getInstance().update(player, new RewardContext(0, false));
			}
		}
	}

	private Optional<CategoryData> getCategoryDataIfUnlocked(ServerPlayerEntity player, CategoryConfig category) {
		return getCategoryDataIfUnlocked(getPlayerData(player), category);
	}

	private Optional<CategoryData> getCategoryDataIfUnlocked(PlayerData playerData, CategoryConfig category) {
		if (playerData.isCategoryUnlocked(category)) {
			return Optional.of(playerData.getCategoryData(category));
		}
		return Optional.empty();
	}

	private Optional<CategoryConfig> getCategory(Identifier categoryId) {
		return categories.get().flatMap(map -> Optional.ofNullable(map.get(categoryId)));
	}

	private Collection<CategoryConfig> getAllCategories() {
		return categories.get().map(Map::values).orElseGet(Collections::emptyList);
	}

	private void syncCategory(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData) {
		applyRewards(player, category, categoryData);
		showCategory(player, category, categoryData);
	}

	private void syncCategory(ServerPlayerEntity player, CategoryConfig category) {
		getCategoryDataIfUnlocked(player, category).ifPresentOrElse(
				categoryData -> syncCategory(player, category, categoryData),
				() -> {
					resetRewards(player, category);
					hideCategory(player, category);
				}
		);
	}

	private void syncAllCategories(ServerPlayerEntity player) {
		if (isConfigValid()) {
			for (var category : getAllCategories()) {
				syncCategory(player, category);
			}
		} else if (isPlayerOperator(player)) {
			packetSender.send(player, InvalidConfigOutPacket.write());
		}
	}

	private boolean isConfigValid() {
		return categories.get().isPresent();
	}

	private PlayerData getPlayerData(ServerPlayerEntity player) {
		return ServerData.getOrCreate(getPlayerServer(player)).getPlayerData(player);
	}

	private MinecraftServer getPlayerServer(ServerPlayerEntity player) {
		return Objects.requireNonNull(player.getServer());
	}

	private boolean isPlayerOperator(ServerPlayerEntity player) {
		return getPlayerServer(player).getPlayerManager().isOperator(player.getGameProfile());
	}

	private class EventListener implements ServerEventListener {

		@Override
		public void onServerStarting(MinecraftServer server) {
			loadModConfig(server);
		}

		@Override
		public void onServerReload(MinecraftServer server) {
			for (var player : server.getPlayerManager().getPlayerList()) {
				for (var category : getAllCategories()) {
					hideCategory(player, category);
				}
			}

			loadModConfig(server);

			for (var player : server.getPlayerManager().getPlayerList()) {
				syncAllCategories(player);
			}
		}

		@Override
		public void onPlayerJoin(ServerPlayerEntity player) {
			syncAllCategories(player);
		}

		@Override
		public void onCommandsRegister(CommandDispatcher<ServerCommandSource> dispatcher) {
			dispatcher.register(CommandManager.literal(SkillsAPI.MOD_ID)
					.then(CategoryCommand.create())
					.then(SkillsCommand.create())
					.then(PointsCommand.create())
					.then(ExperienceCommand.create())
			);
		}
	}
}
