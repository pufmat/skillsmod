package net.puffish.skillsmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.puffish.skillsmod.api.Events;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSource;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.calculation.LegacyBuiltinPrototypes;
import net.puffish.skillsmod.calculation.operation.BuiltinOperations;
import net.puffish.skillsmod.commands.CategoryCommand;
import net.puffish.skillsmod.commands.ExperienceCommand;
import net.puffish.skillsmod.commands.OpenCommand;
import net.puffish.skillsmod.commands.PointsCommand;
import net.puffish.skillsmod.commands.SkillsCommand;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.Config;
import net.puffish.skillsmod.config.ModConfig;
import net.puffish.skillsmod.config.PackConfig;
import net.puffish.skillsmod.config.experience.ExperienceConfig;
import net.puffish.skillsmod.config.reader.ConfigReader;
import net.puffish.skillsmod.config.reader.FileConfigReader;
import net.puffish.skillsmod.config.reader.PackConfigReader;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;
import net.puffish.skillsmod.experience.source.BuiltinExperienceSources;
import net.puffish.skillsmod.impl.config.ConfigContextImpl;
import net.puffish.skillsmod.impl.rewards.RewardUpdateContextImpl;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.reward.BuiltinRewards;
import net.puffish.skillsmod.reward.builtin.PointsReward;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.server.data.PlayerData;
import net.puffish.skillsmod.server.data.ServerData;
import net.puffish.skillsmod.server.event.ServerEventListener;
import net.puffish.skillsmod.server.event.ServerEventReceiver;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import net.puffish.skillsmod.server.network.packets.in.SkillClickInPacket;
import net.puffish.skillsmod.server.network.packets.out.ExperienceUpdateOutPacket;
import net.puffish.skillsmod.server.network.packets.out.HideCategoryOutPacket;
import net.puffish.skillsmod.server.network.packets.out.NewPointOutPacket;
import net.puffish.skillsmod.server.network.packets.out.OpenScreenOutPacket;
import net.puffish.skillsmod.server.network.packets.out.PointsUpdateOutPacket;
import net.puffish.skillsmod.server.network.packets.out.ShowCategoryOutPacket;
import net.puffish.skillsmod.server.network.packets.out.ShowToastOutPacket;
import net.puffish.skillsmod.server.network.packets.out.SkillUpdateOutPacket;
import net.puffish.skillsmod.server.setup.ServerPlatform;
import net.puffish.skillsmod.server.setup.ServerRegistrar;
import net.puffish.skillsmod.server.setup.SkillsArgumentTypes;
import net.puffish.skillsmod.server.setup.SkillsGameRules;
import net.puffish.skillsmod.util.ChangeListener;
import net.puffish.skillsmod.util.DisposeContext;
import net.puffish.skillsmod.util.Event;
import net.puffish.skillsmod.util.PathUtils;
import net.puffish.skillsmod.util.PointSources;
import net.puffish.skillsmod.util.PrefixedLogger;
import net.puffish.skillsmod.util.ToastType;
import net.puffish.skillsmod.util.VersionedConfigContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkillsMod {
	public static final int MIN_CONFIG_VERSION = 1;
	public static final int MAX_CONFIG_VERSION = 3;

	public static final Event<Events.SkillUnlock> SKILL_UNLOCK = Event.create(
			c -> (categoryId, skillId) -> c.forEach(e -> e.onSkillUnlock(categoryId, skillId))
	);
	public static final Event<Events.SkillLock> SKILL_LOCK = Event.create(
			c -> (categoryId, skillId) -> c.forEach(e -> e.onSkillLock(categoryId, skillId))
	);

	private static SkillsMod instance;

	private final PrefixedLogger logger = new PrefixedLogger(SkillsAPI.MOD_ID);

	private final Path modConfigDir;
	private final ServerPacketSender packetSender;
	private final ServerPlatform platform;
	private final SkillsGameRules gameRules;

	private final ChangeListener<Optional<Map<Identifier, CategoryConfig>>> categories = new ChangeListener<>(
			Optional.empty(),
			() -> { }
	);

	private SkillsMod(Path modConfigDir, ServerPacketSender packetSender, ServerPlatform platform, SkillsGameRules gameRules) {
		this.modConfigDir = modConfigDir;
		this.packetSender = packetSender;
		this.platform = platform;
		this.gameRules = gameRules;
	}

	public static SkillsMod getInstance() {
		return instance;
	}

	public ServerPlatform getPlatform() {
		return platform;
	}

	public static void setup(
			Path configDir,
			ServerRegistrar registrar,
			ServerEventReceiver eventReceiver,
			ServerPacketSender packetSender,
			ServerPlatform platform
	) {
		var modConfigDir = configDir.resolve(SkillsAPI.MOD_ID);
		try {
			Files.createDirectories(modConfigDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		var gameRules = SkillsGameRules.register(registrar);

		instance = new SkillsMod(modConfigDir, packetSender, platform, gameRules);

		registrar.registerInPacket(
				Packets.SKILL_CLICK,
				SkillClickInPacket::read,
				instance::onSkillClickPacket
		);

		registrar.registerOutPacket(Packets.SHOW_CATEGORY);
		registrar.registerOutPacket(Packets.HIDE_CATEGORY);
		registrar.registerOutPacket(Packets.SKILL_UPDATE);
		registrar.registerOutPacket(Packets.POINTS_UPDATE);
		registrar.registerOutPacket(Packets.EXPERIENCE_UPDATE);
		registrar.registerOutPacket(Packets.SHOW_TOAST);
		registrar.registerOutPacket(Packets.OPEN_SCREEN);
		registrar.registerOutPacket(Packets.NEW_POINT);

		eventReceiver.registerListener(instance.new EventListener());

		SkillsArgumentTypes.register(registrar);

		BuiltinRewards.register();
		BuiltinOperations.register();
		BuiltinExperienceSources.register();

		LegacyBuiltinPrototypes.register();
	}

	public static Identifier createIdentifier(String path) {
		return Identifier.of(SkillsAPI.MOD_ID, path);
	}

	public static Identifier convertIdentifier(Identifier id) {
		if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
			return createIdentifier(id.getPath());
		}
		return id;
	}

	public static MutableText createTranslatable(String type, String path, Object... args) {
		return Text.stringifiedTranslatable(Util.createTranslationKey(type, createIdentifier(path)), args);
	}

	public PrefixedLogger getLogger() {
		return logger;
	}

	private void copyConfigFromJar() {
		PathUtils.copyFileFromJar(
				Path.of("config", "config.json"),
				modConfigDir.resolve("config.json")
		);
	}

	private void loadModConfig(MinecraftServer server) {
		if (!Files.exists(modConfigDir) || PathUtils.isDirectoryEmpty(modConfigDir)) {
			copyConfigFromJar();
		}

		var reader = new FileConfigReader(modConfigDir);
		var context = new ConfigContextImpl(server);

		reader.read(Path.of("config.json"))
				.andThen(rootElement -> ModConfig.parse(rootElement, context))
				.andThen(modConfig -> loadCategories(reader, modConfig, SkillsAPI.MOD_ID, context)
						.ifSuccess(map -> {
							var cumulatedMap = new LinkedHashMap<>(map);
							showSuccess("Mod configuration", modConfig.showWarnings(), context);

							if (loadPackConfig(server, cumulatedMap, modConfig.showWarnings())) {
								categories.set(Optional.of(cumulatedMap), () -> {
									for (var category : cumulatedMap.values()) {
										category.dispose(new DisposeContext(server));
									}
								});
							} else {
								categories.set(Optional.empty(), () -> { });
							}
						})
				)
				.ifFailure(problem -> {
					categories.set(Optional.empty(), () -> { });
					showFailure("Mod configuration", problem);
				});
	}

	private Result<Map<Identifier, CategoryConfig>, Problem> loadCategories(ConfigReader reader, Config config, String namespace,  ConfigContext context) {
		var versionedContext = new VersionedConfigContext(context, config.version());

		return reader.readCategories(namespace, config.categories(), versionedContext);
	}

	private boolean loadPackConfig(MinecraftServer server, Map<Identifier, CategoryConfig> cumulatedMap, boolean showWarning) {
		var resourceManager = server.getResourceManager();

		var resources = resourceManager.findResources(
				SkillsAPI.MOD_ID,
				id -> id.getPath().endsWith("config.json")
		);

		var allSuccess = true;

		for (var entry : resources.entrySet()) {
			var resource = entry.getValue();
			var id = entry.getKey();
			var namespace = id.getNamespace();
			var reader = new PackConfigReader(resourceManager, namespace);
			var context = new ConfigContextImpl(server);

			if (reader.readResource(id, resource)
					.andThen(rootElement -> PackConfig.parse(namespace, rootElement, context))
					.andThen(packConfig -> loadCategories(reader, packConfig, namespace, context))
					.andThen(map -> {
						var problems = new ArrayList<Problem>();

						for (var key : map.keySet()) {
							if (cumulatedMap.containsKey(key)) {
								problems.add(Problem.message("Category `" + key + "` already exists."));
							}
						}

						if (problems.isEmpty()) {
							return Result.success(map);
						} else {
							return Result.failure(Problem.combine(problems));
						}
					})
					.ifFailure(problem -> showFailure("Data pack `" + namespace + "`", problem))
					.ifSuccess(map -> {
						cumulatedMap.putAll(map);
						showSuccess("Data pack `" + namespace + "`", showWarning, context);
					})
					.getSuccess()
					.isEmpty()) {
				allSuccess = false;
			}
		}

		return allSuccess;
	}

	private void showSuccess(String name, boolean showWarnings, ConfigContextImpl context) {
		if (showWarnings && !context.warnings().isEmpty()) {
			logger.warn(name + " loaded successfully with warning(s):"
					+ System.lineSeparator()
					+ context.warnings().stream().collect(Collectors.joining(System.lineSeparator()))
			);
		} else {
			logger.info(name + " loaded successfully!");
		}
	}

	private void showFailure(String name, Problem problem) {
		logger.error(name + " could not be loaded:"
				+ System.lineSeparator()
				+ problem
		);
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

	public void tryUnlockSkill(ServerPlayerEntity player, Identifier categoryId, String skillId, boolean force) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			category.skills().getById(skillId).ifPresent(skill -> {
				if (categoryData.canUnlockSkill(category, skill, force)) {
					watchNewPoints(player, category, categoryData, false, () -> {
						categoryData.unlockSkill(skillId);
						packetSender.send(player, new SkillUpdateOutPacket(categoryId, skillId, true));
						syncPoints(player, category, categoryData);
					});
					SKILL_UNLOCK.invoker().onSkillUnlock(categoryId, skillId);
					updateSkillRewards(player, category, categoryData, skill, true);
				}
			});
		});
	}

	public void lockSkill(ServerPlayerEntity player, Identifier categoryId, String skillId) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			category.skills().getById(skillId).ifPresent(skill -> {
				watchNewPoints(player, category, categoryData, false, () -> {
					categoryData.lockSkill(skillId);
					packetSender.send(player, new SkillUpdateOutPacket(categoryId, skillId, false));
					syncPoints(player, category, categoryData);
				});
				SKILL_LOCK.invoker().onSkillLock(categoryId, skillId);
				updateSkillRewards(player, category, categoryData, skill, false);
			});
		});
	}

	public void resetSkills(ServerPlayerEntity player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			categoryData.resetSkills();
			updateRewards(player, category, categoryData);
			showCategory(player, category, categoryData);
		});
	}

	public void eraseCategory(ServerPlayerEntity player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var playerData = getPlayerData(player);
			playerData.removeCategoryData(category);

			updateCategory(player, category);
		});
	}

	public void unlockCategory(ServerPlayerEntity player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			categoryData.unlock();
			showCategory(player, category, categoryData);
		});
	}

	public void lockCategory(ServerPlayerEntity player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			categoryData.lock();
			hideCategory(player, category);
		});
	}

	public Optional<Boolean> hasExperience(Identifier categoryId) {
		return getCategory(categoryId).map(category -> category.experience().isPresent());
	}

	public void addExperience(ServerPlayerEntity player, Identifier categoryId, int amount) {
		getCategory(categoryId).ifPresent(category -> {
			category.experience().ifPresent(experience -> {
				var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
				addExperience(player, category, experience, categoryData, amount);
			});
		});
	}

	public void addExperience(ServerPlayerEntity player, CategoryConfig category, ExperienceConfig experience, CategoryData categoryData, int amount) {
		setExperience(player, category, experience, categoryData, categoryData.getExperience() + amount);
	}

	public void setExperience(ServerPlayerEntity player, Identifier categoryId, int amount) {
		getCategory(categoryId).ifPresent(category -> {
			category.experience().ifPresent(experience -> {
				var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
				setExperience(player, category, experience, categoryData, amount);
			});
		});
	}

	public void setExperience(ServerPlayerEntity player, CategoryConfig category, ExperienceConfig experience, CategoryData categoryData, int amount) {
		var curve = experience.curve();
		var level = curve.getProgress(amount).currentLevel();
		var levelLimit = curve.getLevelLimit();
		if (level >= levelLimit) {
			level = levelLimit;
			amount = curve.getRequiredTotal(levelLimit - 1);
		}
		categoryData.setExperience(amount);

		syncExperience(player, category, experience, categoryData);
		setPoints(player, category, categoryData, PointSources.EXPERIENCE, level, false);
	}

	public Optional<Integer> getExperience(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).flatMap(category -> {
			if (category.experience().isEmpty()) {
				return Optional.empty();
			}

			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return Optional.of(categoryData.getExperience());
		});
	}

	public void addPoints(ServerPlayerEntity player, Identifier categoryId, Identifier source, int count, boolean isSilent) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			addPoints(player, category, categoryData, source, count, isSilent);
		});
	}

	public void addPoints(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData, Identifier source, int count, boolean isSilent) {
		setPoints(player, category, categoryData, source, categoryData.getPoints(source) + count, isSilent);
	}

	public void setPoints(ServerPlayerEntity player, Identifier categoryId, Identifier source, int count, boolean isSilent) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			setPoints(player, category, categoryData, source, count, isSilent);
		});
	}

	public void setPoints(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData, Identifier source, int count, boolean isSilent) {
		watchNewPoints(player, category, categoryData, isSilent, () -> {
			categoryData.setPoints(source, count);

			syncPoints(player, category, categoryData);
		});
	}

	public Optional<Integer> getPoints(ServerPlayerEntity player, Identifier categoryId, Identifier source) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getPoints(source);
		});
	}

	public Optional<Integer> getPointsTotal(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getPointsTotal();
		});
	}

	public Optional<Stream<Identifier>> getPointsSources(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getPointsSources();
		});
	}

	public Optional<Integer> getPointsLeft(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getPointsLeft(category);
		});
	}

	public Optional<Integer> getSpentPoints(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getSpentPoints(category);
		});
	}

	public Optional<Integer> getCurrentLevel(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> category.experience()
				.map(experience -> {
					var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
					return experience.curve().getProgress(categoryData.getExperience()).currentLevel();
				})
				.orElse(0));
	}

	public Optional<Integer> getCurrentExperience(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> category.experience()
				.map(experience -> {
					var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
					return experience.curve().getProgress(categoryData.getExperience()).currentExperience();
				})
				.orElse(0));
	}

	public Optional<Integer> getRequiredExperience(Identifier categoryId, int level) {
		return getCategory(categoryId).map(category -> category.experience()
				.map(experience -> experience.curve().getRequired(level))
				.orElse(0));
	}

	public Optional<Integer> getRequiredTotalExperience(Identifier categoryId, int level) {
		return getCategory(categoryId).map(category -> category.experience()
				.map(experience -> experience.curve().getRequiredTotal(level))
				.orElse(0));
	}

	public Optional<Skill.State> getSkillState(ServerPlayerEntity player, Identifier categoryId, String skillId) {
		return getCategory(categoryId).flatMap(category -> category.skills()
				.getById(skillId)
				.flatMap(skill -> category.definitions()
						.getById(skill.definitionId())
						.map(definition -> {
							var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
							return categoryData.getSkillState(category, skill, definition);
						})
				)
		);
	}

	public Collection<Identifier> getUnlockedCategories(ServerPlayerEntity player) {
		var playerData = getPlayerData(player);

		return getAllCategories()
				.stream()
				.filter(playerData::isCategoryUnlocked)
				.map(CategoryConfig::id)
				.toList();
	}

	public Collection<Identifier> getCategories(boolean onlyWithExperience) {
		return getAllCategories()
				.stream()
				.filter(category -> !onlyWithExperience || category.experience().isPresent())
				.map(CategoryConfig::id)
				.toList();
	}

	public Optional<Collection<String>> getUnlockedSkills(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getUnlockedSkillIds();
		});
	}

	public Optional<Collection<String>> getSkills(Identifier categoryId) {
		return getCategory(categoryId).map(
				category -> category.skills()
						.getAll()
						.stream()
						.map(SkillConfig::id)
						.toList()
		);
	}

	public boolean hasCategory(Identifier categoryId) {
		return getCategory(categoryId).isPresent();
	}

	public boolean hasSkill(Identifier categoryId, String skillId) {
		return getCategory(categoryId)
				.map(category -> category.skills().getById(skillId).isPresent())
				.orElse(false);
	}

	private void showCategory(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData) {
		updatePoints(category, categoryData);
		updateRewards(player, category, categoryData);
		packetSender.send(player, new ShowCategoryOutPacket(category, categoryData));
	}

	private void hideCategory(ServerPlayerEntity player, CategoryConfig category) {
		resetRewards(player, category);
		packetSender.send(player, new HideCategoryOutPacket(category.id()));
	}

	public void exportPlayerData(ServerPlayerEntity player, NbtCompound nbt) {
		getPlayerData(player).writeNbt(nbt);
	}

	public void importPlayerData(ServerPlayerEntity player, NbtCompound nbt) {
		for (var category : getAllCategories()) {
			resetRewards(player, category);
		}
		putPlayerData(player, PlayerData.read(nbt));
		updateAllCategories(player);
	}

	private void watchNewPoints(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData, boolean isSilent, Runnable runnable) {
		if (isSilent) {
			runnable.run();
		} else {
			var pointsLeft = categoryData.getPointsLeft(category);
			runnable.run();
			if (categoryData.getPointsLeft(category) > pointsLeft) {
				if (player.getServerWorld().getGameRules().getBoolean(gameRules.announceNewPoints())) {
					packetSender.send(player, new NewPointOutPacket(category.id()));
				}
			}
		}
	}

	private void syncPoints(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData) {
		packetSender.send(player, new PointsUpdateOutPacket(
				category.id(),
				categoryData.getSpentPoints(category),
				categoryData.getPointsTotal()
		));
	}

	private void syncExperience(ServerPlayerEntity player, CategoryConfig category, ExperienceConfig experience, CategoryData categoryData) {
		var progress = experience.curve().getProgress(categoryData.getExperience());
		packetSender.send(player, new ExperienceUpdateOutPacket(
				category.id(),
				progress.currentLevel(),
				progress.currentExperience(),
				progress.requiredExperience()
		));
	}

	public void visitExperienceSources(ServerPlayerEntity player, Function<ExperienceSource, Integer> function) {
		if (platform.isFakePlayer(player)) {
			return;
		}

		var playerData = getPlayerData(player);
		for (var category : getAllCategories()) {
			if (!playerData.isCategoryUnlocked(category)) {
				continue;
			}

			category.experience().ifPresent(experience -> visitExperienceSources(
					player, playerData, category, experience, function
			));
		}
	}

	private void visitExperienceSources(ServerPlayerEntity player, PlayerData playerData, CategoryConfig category, ExperienceConfig experience, Function<ExperienceSource, Integer> function) {
		var amount = 0;
		var teamAmounts = new HashMap<ServerPlayerEntity, Integer>();

		for (var experienceSource : experience.experienceSources()) {
			var result = function.apply(experienceSource.instance());
			if (result == 0) {
				continue;
			}
			amount += result;

			experienceSource.teamSharing().ifPresent(teamSharing -> {
				var teamPlayers = player.getServerWorld().getPlayers(
						otherPlayer -> player != otherPlayer
								&& player.isTeammate(otherPlayer)
								&& player.distanceTo(otherPlayer) <= teamSharing.distanceLimit()
								&& getPlayerData(otherPlayer).isCategoryUnlocked(category)
				);
				for (var teamPlayer : teamPlayers) {
					teamAmounts.compute(teamPlayer, (key, value) -> (value == null ? 0 : value) + result);
				}
			});
		}

		if (amount != 0) {
			var categoryData = playerData.getOrCreateCategoryData(category);
			addExperience(player, category, experience, categoryData, amount);
		}
		teamAmounts.forEach((teamPlayer, teamPlayerAmount) -> {
			var categoryData = getPlayerData(teamPlayer).getOrCreateCategoryData(category);
			addExperience(teamPlayer, category, experience, categoryData, teamPlayerAmount);
		});
	}

	public void updateRewards(ServerPlayerEntity player, Predicate<SkillRewardConfig> predicate) {
		if (platform.isFakePlayer(player)) {
			return;
		}

		var playerData = getPlayerData(player);
		for (var category : getAllCategories()) {
			getCategoryDataIfUnlocked(playerData, category).ifPresent(categoryData -> {
				for (var definition : category.definitions().getAll()) {
					var count = categoryData.countUnlocked(category, definition.id());

					for (var reward : definition.rewards()) {
						if (predicate.test(reward)) {
							reward.instance().update(new RewardUpdateContextImpl(player, count, false));
						}
					}
				}
			});
		}
	}

	private void updateRewards(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData) {
		for (var definition : category.definitions().getAll()) {
			var count = categoryData.countUnlocked(category, definition.id());

			for (var reward : definition.rewards()) {
				reward.instance().update(new RewardUpdateContextImpl(player, count, false));
			}
		}
	}

	private void updateSkillRewards(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData, SkillConfig skill, boolean isUnlock) {
		category.definitions().getById(skill.definitionId()).ifPresent(definition -> {
			var count = categoryData.countUnlocked(category, definition.id());

			for (var reward : definition.rewards()) {
				reward.instance().update(new RewardUpdateContextImpl(player, count, isUnlock));
			}
		});
	}

	private void resetRewards(ServerPlayerEntity player, CategoryConfig category) {
		for (var definition : category.definitions().getAll()) {
			for (var reward : definition.rewards()) {
				reward.instance().update(new RewardUpdateContextImpl(player, 0, false));
			}
		}
	}

	private Optional<CategoryData> getCategoryDataIfUnlocked(ServerPlayerEntity player, CategoryConfig category) {
		return getCategoryDataIfUnlocked(getPlayerData(player), category);
	}

	private Optional<CategoryData> getCategoryDataIfUnlocked(PlayerData playerData, CategoryConfig category) {
		if (playerData.isCategoryUnlocked(category)) {
			return Optional.of(playerData.getOrCreateCategoryData(category));
		}
		return Optional.empty();
	}

	public Optional<Boolean> isCategoryUnlocked(ServerPlayerEntity player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> getPlayerData(player).isCategoryUnlocked(category));
	}

	private Optional<CategoryConfig> getCategory(Identifier categoryId) {
		return categories.get().flatMap(map -> Optional.ofNullable(map.get(categoryId)));
	}

	private Collection<CategoryConfig> getAllCategories() {
		return categories.get().map(Map::values).orElseGet(Collections::emptyList);
	}

	private void updatePoints(CategoryConfig category, CategoryData categoryData) {
		categoryData.setPoints(PointSources.STARTING, category.general().startingPoints());
		category.experience().ifPresent(experience -> {
			categoryData.setPoints(PointSources.EXPERIENCE, experience.curve().getProgress(categoryData.getExperience()).currentLevel());
		});

		var legacy = categoryData.getPoints(PointSources.LEGACY);
		if (legacy != 0) {
			categoryData.setPoints(PointSources.LEGACY, 0);
			categoryData.setPoints(PointSources.COMMANDS, legacy - category.general().startingPoints());
		}
	}

	private void updateCategory(ServerPlayerEntity player, CategoryConfig category) {
		getCategoryDataIfUnlocked(player, category).ifPresentOrElse(
				categoryData -> showCategory(player, category, categoryData),
				() -> hideCategory(player, category)
		);
	}

	public void updateAllCategories(ServerPlayerEntity player) {
		if (isConfigValid()) {
			var categories = getAllCategories();
			if (categories.isEmpty()) {
				showToast(player, ToastType.MISSING_CONFIG);
			} else {
				for (var category : categories) {
					updateCategory(player, category);
				}
			}
		} else {
			showToast(player, ToastType.INVALID_CONFIG);
		}
	}

	private void showToast(ServerPlayerEntity player, ToastType type) {
		if (isOperatorOrHost(player)) {
			packetSender.send(player, new ShowToastOutPacket(type));
		}
	}

	public void openScreen(ServerPlayerEntity player, Optional<Identifier> categoryId) {
		packetSender.send(player, new OpenScreenOutPacket(categoryId));
	}

	private boolean isConfigValid() {
		return categories.get().isPresent();
	}

	private PlayerData getPlayerData(ServerPlayerEntity player) {
		return ServerData.getOrCreate(getPlayerServer(player)).getPlayerData(player);
	}

	private void putPlayerData(ServerPlayerEntity player, PlayerData playerData) {
		ServerData.getOrCreate(getPlayerServer(player)).putPlayerData(player, playerData);
	}

	public MinecraftServer getPlayerServer(ServerPlayerEntity player) {
		return player.server;
	}

	private boolean isOperatorOrHost(ServerPlayerEntity player) {
		var server = getPlayerServer(player);
		return server.isHost(player.getGameProfile())
				|| server.getPlayerManager().isOperator(player.getGameProfile());
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
				updateAllCategories(player);
			}
		}

		@Override
		public void onPlayerJoin(ServerPlayerEntity player) {
			PointsReward.cleanup(player);
			updateAllCategories(player);
		}

		@Override
		public void onPlayerLeave(ServerPlayerEntity player) {
			for (var category : getAllCategories()) {
				resetRewards(player, category);
			}
		}

		@Override
		public void onCommandsRegister(CommandDispatcher<ServerCommandSource> dispatcher) {
			dispatcher.register(CommandManager.literal(SkillsAPI.MOD_ID)
					.then(CategoryCommand.create())
					.then(SkillsCommand.create())
					.then(PointsCommand.create())
					.then(ExperienceCommand.create())
					.then(OpenCommand.create())
			);
		}
	}
}
