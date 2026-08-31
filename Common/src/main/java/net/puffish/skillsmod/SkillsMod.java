package net.puffish.skillsmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
import net.puffish.skillsmod.commands.LevelCommand;
import net.puffish.skillsmod.commands.OpenCommand;
import net.puffish.skillsmod.commands.PointsCommand;
import net.puffish.skillsmod.commands.SkillsCommand;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.Config;
import net.puffish.skillsmod.config.ExchangeConfig;
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
import net.puffish.skillsmod.mixin.ServerPlayerAccessor;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.reward.BuiltinRewards;
import net.puffish.skillsmod.reward.builtin.PointsReward;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.server.data.PlayerData;
import net.puffish.skillsmod.server.data.ServerData;
import net.puffish.skillsmod.server.event.ServerEventListener;
import net.puffish.skillsmod.server.event.ServerEventReceiver;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import net.puffish.skillsmod.server.network.packets.in.BuyPointInPacket;
import net.puffish.skillsmod.server.network.packets.in.SkillClickInPacket;
import net.puffish.skillsmod.server.network.packets.out.ExchangeUpdateOutPacket;
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
import net.puffish.skillsmod.server.setup.SkillsTriggers;
import net.puffish.skillsmod.server.setup.SkillsGameRules;
import net.puffish.skillsmod.util.CategoryFilter;
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
			c -> (player, categoryId, skillId) -> c.forEach(e -> e.onSkillUnlock(player, categoryId, skillId))
	);
	public static final Event<Events.SkillLock> SKILL_LOCK = Event.create(
			c -> (player, categoryId, skillId) -> c.forEach(e -> e.onSkillLock(player, categoryId, skillId))
	);

	private static SkillsMod instance;

	private final PrefixedLogger logger = new PrefixedLogger(SkillsAPI.MOD_ID);

	private final Path modConfigDir;
	private final ServerPacketSender packetSender;
	private final ServerPlatform platform;

	private final ChangeListener<Optional<Map<Identifier, CategoryConfig>>> categories = new ChangeListener<>(
			Optional.empty(),
			() -> { }
	);

	private SkillsMod(Path modConfigDir, ServerPacketSender packetSender, ServerPlatform platform) {
		this.modConfigDir = modConfigDir;
		this.packetSender = packetSender;
		this.platform = platform;
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

		instance = new SkillsMod(modConfigDir, packetSender, platform);

		registrar.registerInPacket(
				Packets.SKILL_CLICK,
				SkillClickInPacket::read,
				instance::onSkillClickPacket
		);

		registrar.registerInPacket(
				Packets.BUY_POINT,
				BuyPointInPacket::read,
				instance::onBuyPointPacket
		);

		registrar.registerOutPacket(Packets.SHOW_CATEGORY);
		registrar.registerOutPacket(Packets.HIDE_CATEGORY);
		registrar.registerOutPacket(Packets.SKILL_UPDATE);
		registrar.registerOutPacket(Packets.POINTS_UPDATE);
		registrar.registerOutPacket(Packets.EXPERIENCE_UPDATE);
		registrar.registerOutPacket(Packets.EXCHANGE_UPDATE);
		registrar.registerOutPacket(Packets.SHOW_TOAST);
		registrar.registerOutPacket(Packets.OPEN_SCREEN);
		registrar.registerOutPacket(Packets.NEW_POINT);

		eventReceiver.registerListener(instance.new EventListener());

		SkillsGameRules.register(registrar);
		SkillsArgumentTypes.register(registrar);
		SkillsTriggers.register(registrar);

		BuiltinRewards.register();
		BuiltinOperations.register();
		BuiltinExperienceSources.register();

		LegacyBuiltinPrototypes.register();
	}

	public static Identifier createIdentifier(String path) {
		return Identifier.fromNamespaceAndPath(SkillsAPI.MOD_ID, path);
	}

	public static Identifier convertIdentifier(Identifier id) {
		if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
			return createIdentifier(id.getPath());
		}
		return id;
	}

	public static MutableComponent createTranslatable(String type, String path, Object... args) {
		return Component.translatableEscape(Util.makeDescriptionId(type, createIdentifier(path)), args);
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
				.andThen(modConfig -> loadCategories(reader, modConfig, SkillsAPI.MOD_ID, 0, context)
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

	private Result<Map<Identifier, CategoryConfig>, Problem> loadCategories(ConfigReader reader, Config config, String namespace, int position, ConfigContext context) {
		var versionedContext = new VersionedConfigContext(context, config.version());

		return reader.readCategories(namespace, config.categories(), position, versionedContext);
	}

	private boolean loadPackConfig(MinecraftServer server, Map<Identifier, CategoryConfig> cumulatedMap, boolean showWarning) {
		var resourceManager = server.getResourceManager();

		var resources = resourceManager.listResources(
				SkillsAPI.MOD_ID,
				id -> id.getPath().endsWith("config.json")
		);

		var allSuccess = true;

		for (var entry : resources.entrySet()) {
			var resource = entry.getValue();
			var id = entry.getKey();
			var namespace = id.getNamespace();
			var position = cumulatedMap.size();
			var reader = new PackConfigReader(resourceManager, namespace);
			var context = new ConfigContextImpl(server);

			if (reader.readResource(id, resource)
					.andThen(rootElement -> PackConfig.parse(namespace, rootElement, context))
					.andThen(packConfig -> loadCategories(reader, packConfig, namespace, position, context))
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

	private void onBuyPointPacket(ServerPlayer player, BuyPointInPacket packet) {
		if (player.isSpectator()) {
			return;
		}
		tryBuyPoint(player, packet.getCategoryId());
	}

	private void tryBuyPoint(ServerPlayer player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			category.exchange().ifPresent(exchange -> {
				var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
				var level = categoryData.getExchangeLevel();
				if (level < exchange.levelLimit()) {
					var cost = exchange.function().apply(level);
					if (player.experienceLevel >= cost) {
						player.giveExperienceLevels(-cost);
						// vanilla does not send this immediately, so it is sent manually here
						player.connection.send(new ClientboundSetExperiencePacket(
								player.experienceProgress,
								player.totalExperience,
								player.experienceLevel
						));

						level += 1;
						categoryData.setExchangeLevel(level);
						setPoints(player, category, categoryData, PointSources.EXCHANGE, level, true);
						syncExchange(player, category, exchange, categoryData);
					}
				}
			});
		});
	}

	private void onSkillClickPacket(ServerPlayer player, SkillClickInPacket packet) {
		if (player.isSpectator()) {
			return;
		}
		tryUnlockSkill(player, packet.getCategoryId(), packet.getSkillId(), false);
	}

	public void onPlayerDeath(ServerPlayer player) {
		if (platform.isFakePlayer(player)) {
			return;
		}
		var playerData = getPlayerData(player);
		for (var category : getAllCategories()) {
			var categoryData = playerData.getOrCreateCategoryData(category);
			category.experience().ifPresent(experience -> {
				if (experience.resetOnDeath()) {
					var amount = categoryData.getExperience();
					amount -= experience.curve().getProgress(amount).currentExperience();
					setExperience(player, category, experience, categoryData, amount);
				}
			});
			if (category.general().eraseOnDeath()) {
				eraseCategory(player, playerData, category);
			}
		}
	}

	public void unlockSkill(ServerPlayer player, Identifier categoryId, String skillId) {
		tryUnlockSkill(player, categoryId, skillId, true);
	}

	public void tryUnlockSkill(ServerPlayer player, Identifier categoryId, String skillId, boolean force) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			category.skills().getById(skillId).ifPresent(skill -> {
				if (categoryData.canUnlockSkill(category, skill, force)) {
					watchNewPoints(player, category, categoryData, false, () -> {
						categoryData.unlockSkill(skillId);
						packetSender.send(player, new SkillUpdateOutPacket(categoryId, skillId, true));
						syncPoints(player, category, categoryData);
					});
					SKILL_UNLOCK.invoker().onSkillUnlock(player, categoryId, skillId);
					updateSkillRewards(player, category, categoryData, skill, true);
				}
			});
		});
	}

	public void lockSkill(ServerPlayer player, Identifier categoryId, String skillId) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			category.skills().getById(skillId).ifPresent(skill -> {
				watchNewPoints(player, category, categoryData, false, () -> {
					categoryData.lockSkill(skillId);
					packetSender.send(player, new SkillUpdateOutPacket(categoryId, skillId, false));
					syncPoints(player, category, categoryData);
				});
				SKILL_LOCK.invoker().onSkillLock(player, categoryId, skillId);
				updateSkillRewards(player, category, categoryData, skill, false);
			});
		});
	}

	public void resetSkills(ServerPlayer player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			resetSkills(player, category, getPlayerData(player).getOrCreateCategoryData(category));
		});
	}

	private void resetSkills(ServerPlayer player, CategoryConfig category, CategoryData categoryData) {
		var unlockedSkillIds = new ArrayList<>(categoryData.getUnlockedSkillIds());

		categoryData.resetSkills();
		updateRewards(player, category, categoryData);
		showCategory(player, category, categoryData);

		for (var skillId : unlockedSkillIds) {
			SKILL_LOCK.invoker().onSkillLock(player, category.id(), skillId);
		}
	}

	public void eraseCategory(ServerPlayer player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var playerData = getPlayerData(player);
			eraseCategory(player, playerData, category);
		});
	}

	private void eraseCategory(ServerPlayer player, PlayerData playerData, CategoryConfig category) {
		playerData.removeCategoryData(category);

		updateCategory(player, category);
	}

	public void unlockCategory(ServerPlayer player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			categoryData.unlock();
			showCategory(player, category, categoryData);
		});
	}

	public void lockCategory(ServerPlayer player, Identifier categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			categoryData.lock();
			hideCategory(player, category);
		});
	}

	public Optional<Boolean> hasExchange(Identifier categoryId) {
		return getCategory(categoryId).map(category -> category.exchange().isPresent());
	}

	public Optional<Boolean> hasExperience(Identifier categoryId) {
		return getCategory(categoryId).map(category -> category.experience().isPresent());
	}

	public void addExperience(ServerPlayer player, Identifier categoryId, int amount) {
		getCategory(categoryId).ifPresent(category -> {
			category.experience().ifPresent(experience -> {
				var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
				addExperience(player, category, experience, categoryData, amount);
			});
		});
	}

	public void addExperience(ServerPlayer player, CategoryConfig category, ExperienceConfig experience, CategoryData categoryData, int amount) {
		setExperience(player, category, experience, categoryData, categoryData.getExperience() + amount);
	}

	public void setExperience(ServerPlayer player, Identifier categoryId, int amount) {
		getCategory(categoryId).ifPresent(category -> {
			category.experience().ifPresent(experience -> {
				var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
				setExperience(player, category, experience, categoryData, amount);
			});
		});
	}

	public void setExperience(ServerPlayer player, CategoryConfig category, ExperienceConfig experience, CategoryData categoryData, int amount) {
		if (amount < 0) {
			amount = 0;
		}
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

	public Optional<Integer> getExperience(ServerPlayer player, Identifier categoryId) {
		return getCategory(categoryId).flatMap(category -> {
			if (category.experience().isEmpty()) {
				return Optional.empty();
			}

			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return Optional.of(categoryData.getExperience());
		});
	}

	public Optional<Integer> getExchangeLevel(ServerPlayer player, Identifier categoryId) {
		return getCategory(categoryId).flatMap(category -> {
			if (category.exchange().isEmpty()) {
				return Optional.empty();
			}

			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return Optional.of(categoryData.getExchangeLevel());
		});
	}

	public void addExchangeLevel(ServerPlayer player, Identifier categoryId, int count) {
		getCategory(categoryId).ifPresent(category -> {
			category.exchange().ifPresent(exchange -> {
				var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
				addExchangeLevel(player, category, exchange, categoryData, count);
			});
		});
	}

	private void addExchangeLevel(ServerPlayer player, CategoryConfig category, ExchangeConfig exchange, CategoryData categoryData, int count) {
		setExchangeLevel(player, category, exchange, categoryData, categoryData.getExchangeLevel() + count);
	}

	public void setExchangeLevel(ServerPlayer player, Identifier categoryId, int level) {
		getCategory(categoryId).ifPresent(category -> {
			category.exchange().ifPresent(exchange -> {
				var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
				setExchangeLevel(player, category, exchange, categoryData, level);
			});
		});
	}

	private void setExchangeLevel(ServerPlayer player, CategoryConfig category, ExchangeConfig exchange, CategoryData categoryData, int level) {
		if (level < 0) {
			level = 0;
		}

		var levelLimit = exchange.levelLimit();
		if (level >= levelLimit) {
			level = levelLimit;
		}
		categoryData.setExchangeLevel(level);
		syncExchange(player, category, exchange, categoryData);
	}

	public void addPoints(ServerPlayer player, Identifier categoryId, Identifier source, int count, boolean isSilent) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			addPoints(player, category, categoryData, source, count, isSilent);
		});
	}

	private void addPoints(ServerPlayer player, CategoryConfig category, CategoryData categoryData, Identifier source, int count, boolean isSilent) {
		setPoints(player, category, categoryData, source, categoryData.getPoints(source) + count, isSilent);
	}

	public void setPoints(ServerPlayer player, Identifier categoryId, Identifier source, int count, boolean isSilent) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			setPoints(player, category, categoryData, source, count, isSilent);
		});
	}

	private void setPoints(ServerPlayer player, CategoryConfig category, CategoryData categoryData, Identifier source, int count, boolean isSilent) {
		watchNewPoints(player, category, categoryData, isSilent, () -> {
			categoryData.setPoints(source, count);

			syncPoints(player, category, categoryData);
		});
	}

	public Optional<Integer> getPoints(ServerPlayer player, Identifier categoryId, Identifier source) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getPoints(source);
		});
	}

	public Optional<Integer> getPointsTotal(ServerPlayer player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getPointsTotal();
		});
	}

	public Optional<Stream<Identifier>> getPointsSources(ServerPlayer player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getPointsSources();
		});
	}

	public Optional<Integer> getPointsLeft(ServerPlayer player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getPointsLeft(category);
		});
	}

	public Optional<Integer> getSpentPoints(ServerPlayer player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
			return categoryData.getSpentPoints(category);
		});
	}

	public Optional<Integer> getCost(Identifier categoryId, int level) {
		return getCategory(categoryId).map(category -> category.exchange()
				.map(exchange -> exchange.function().apply(level))
				.orElse(0));
	}

	public Optional<Integer> getExperienceLevel(ServerPlayer player, Identifier categoryId) {
		return getCategory(categoryId).map(category -> category.experience()
				.map(experience -> {
					var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
					return getExperienceLevel(experience, categoryData);
				})
				.orElse(0));
	}

	private int getExperienceLevel(ExperienceConfig experience, CategoryData categoryData) {
		return experience.curve().getProgress(categoryData.getExperience()).currentLevel();
	}

	public void setExperienceLevel(ServerPlayer player, Identifier categoryId, int level) {
		getCategory(categoryId).ifPresent(category -> {
			category.experience().ifPresent(experience -> {
				var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
				setExperienceLevel(player, category, experience, categoryData, level);
			});
		});
	}

	private void setExperienceLevel(ServerPlayer player, CategoryConfig category, ExperienceConfig experience, CategoryData categoryData, int level) {
		var curve = experience.curve();
		var progress = curve.getProgress(categoryData.getExperience());
		var amount = curve.getRequiredTotal(level - 1);
		if (progress.requiredExperience() > 0) {
			amount += curve.getRequired(level) * progress.currentExperience() / progress.requiredExperience();
		}
		setExperience(player, category, experience, categoryData, amount);
	}

	public void addExperienceLevel(ServerPlayer player, Identifier categoryId, int count) {
		getCategory(categoryId).ifPresent(category -> {
			category.experience().ifPresent(experience -> {
				var categoryData = getPlayerData(player).getOrCreateCategoryData(category);
				addExperienceLevel(player, category, experience, categoryData, count);
			});
		});
	}

	private void addExperienceLevel(ServerPlayer player, CategoryConfig category, ExperienceConfig experience, CategoryData categoryData, int count) {
		setExperienceLevel(player, category, experience, categoryData, getExperienceLevel(experience, categoryData) + count);
	}

	public Optional<Integer> getCurrentExperience(ServerPlayer player, Identifier categoryId) {
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

	public Optional<Skill.State> getSkillState(ServerPlayer player, Identifier categoryId, String skillId) {
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

	public Collection<Identifier> getUnlockedCategories(ServerPlayer player) {
		var playerData = getPlayerData(player);

		return getAllCategories()
				.stream()
				.filter(playerData::isCategoryUnlocked)
				.map(CategoryConfig::id)
				.toList();
	}

	public Collection<Identifier> getCategories(CategoryFilter filter) {
		return getAllCategories()
				.stream()
				.filter(category -> switch (filter) {
					case ALL -> true;
					case WITH_EXPERIENCE -> category.experience().isPresent();
					case WITH_LEVEL -> category.experience().isPresent() || category.exchange().isPresent();
				})
				.map(CategoryConfig::id)
				.toList();
	}

	public Optional<Collection<String>> getUnlockedSkills(ServerPlayer player, Identifier categoryId) {
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

	private void showCategory(ServerPlayer player, CategoryConfig category, CategoryData categoryData) {
		updatePoints(category, categoryData);
		updateRewards(player, category, categoryData);
		packetSender.send(player, new ShowCategoryOutPacket(category, categoryData));
	}

	private void hideCategory(ServerPlayer player, CategoryConfig category) {
		resetRewards(player, category);
		packetSender.send(player, new HideCategoryOutPacket(category.id()));
	}

	public void exportPlayerData(ServerPlayer player, CompoundTag nbt) {
		getPlayerData(player).writeNbt(nbt);
	}

	public void importPlayerData(ServerPlayer player, CompoundTag nbt) {
		for (var category : getAllCategories()) {
			resetRewards(player, category);
		}
		putPlayerData(player, PlayerData.read(nbt));
		updateAllCategories(player);
	}

	private void watchNewPoints(ServerPlayer player, CategoryConfig category, CategoryData categoryData, boolean isSilent, Runnable runnable) {
		if (isSilent) {
			runnable.run();
		} else {
			var pointsLeft = categoryData.getPointsLeft(category);
			runnable.run();
			if (categoryData.getPointsLeft(category) > pointsLeft) {
				if (player.level().getGameRules().get(SkillsGameRules.ANNOUNCE_NEW_POINTS)) {
					packetSender.send(player, new NewPointOutPacket(category.id()));
				}
			}
		}
	}

	private void syncPoints(ServerPlayer player, CategoryConfig category, CategoryData categoryData) {
		packetSender.send(player, new PointsUpdateOutPacket(
				category.id(),
				categoryData.getSpentPoints(category),
				categoryData.getPointsTotal()
		));
	}

	private void syncExperience(ServerPlayer player, CategoryConfig category, ExperienceConfig experience, CategoryData categoryData) {
		var progress = experience.curve().getProgress(categoryData.getExperience());
		packetSender.send(player, new ExperienceUpdateOutPacket(
				category.id(),
				progress.currentLevel(),
				progress.currentExperience(),
				progress.requiredExperience()
		));
	}

	private void syncExchange(ServerPlayer player, CategoryConfig category, ExchangeConfig exchange, CategoryData categoryData) {
		var level = categoryData.getExchangeLevel();
		packetSender.send(player, new ExchangeUpdateOutPacket(
				category.id(),
				level,
				exchange.function().apply(level)
		));
	}

	public void visitExperienceSources(ServerPlayer player, Function<ExperienceSource, Integer> function) {
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

	private void visitExperienceSources(ServerPlayer player, PlayerData playerData, CategoryConfig category, ExperienceConfig experience, Function<ExperienceSource, Integer> function) {
		var amount = 0;
		var teamAmounts = new HashMap<ServerPlayer, Integer>();

		for (var experienceSource : experience.experienceSources()) {
			var result = function.apply(experienceSource.instance());
			if (result == 0) {
				continue;
			}
			amount += result;

			experienceSource.teamSharing().ifPresent(teamSharing -> {
				var teamPlayers = player.level().getPlayers(
						otherPlayer -> player != otherPlayer
								&& player.isAlliedTo(otherPlayer)
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

	public void updateRewards(ServerPlayer player, Predicate<SkillRewardConfig> predicate) {
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

	private void updateRewards(ServerPlayer player, CategoryConfig category, CategoryData categoryData) {
		for (var definition : category.definitions().getAll()) {
			var count = categoryData.countUnlocked(category, definition.id());

			for (var reward : definition.rewards()) {
				reward.instance().update(new RewardUpdateContextImpl(player, count, false));
			}
		}
	}

	private void updateSkillRewards(ServerPlayer player, CategoryConfig category, CategoryData categoryData, SkillConfig skill, boolean isUnlock) {
		category.definitions().getById(skill.definitionId()).ifPresent(definition -> {
			var count = categoryData.countUnlocked(category, definition.id());

			for (var reward : definition.rewards()) {
				reward.instance().update(new RewardUpdateContextImpl(player, count, isUnlock));
			}
		});
	}

	private void resetRewards(ServerPlayer player, CategoryConfig category) {
		for (var definition : category.definitions().getAll()) {
			for (var reward : definition.rewards()) {
				reward.instance().update(new RewardUpdateContextImpl(player, 0, false));
			}
		}
	}

	private Optional<CategoryData> getCategoryDataIfUnlocked(ServerPlayer player, CategoryConfig category) {
		return getCategoryDataIfUnlocked(getPlayerData(player), category);
	}

	private Optional<CategoryData> getCategoryDataIfUnlocked(PlayerData playerData, CategoryConfig category) {
		if (playerData.isCategoryUnlocked(category)) {
			return Optional.of(playerData.getOrCreateCategoryData(category));
		}
		return Optional.empty();
	}

	public Optional<Boolean> isCategoryUnlocked(ServerPlayer player, Identifier categoryId) {
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

	private void updateCategory(ServerPlayer player, CategoryConfig category) {
		getCategoryDataIfUnlocked(player, category).ifPresentOrElse(
				categoryData -> showCategory(player, category, categoryData),
				() -> hideCategory(player, category)
		);
	}

	public void updateAllCategories(ServerPlayer player) {
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

	private void showToast(ServerPlayer player, ToastType type) {
		if (isOperatorOrHost(player)) {
			packetSender.send(player, new ShowToastOutPacket(type));
		}
	}

	public void openScreen(ServerPlayer player, Optional<Identifier> categoryId) {
		packetSender.send(player, new OpenScreenOutPacket(categoryId));
	}

	private boolean isConfigValid() {
		return categories.get().isPresent();
	}

	private PlayerData getPlayerData(ServerPlayer player) {
		return ServerData.getOrCreate(getPlayerServer(player)).getPlayerData(player);
	}

	private void putPlayerData(ServerPlayer player, PlayerData playerData) {
		ServerData.getOrCreate(getPlayerServer(player)).putPlayerData(player, playerData);
	}

	public MinecraftServer getPlayerServer(ServerPlayer player) {
		return ((ServerPlayerAccessor) player).getServer();
	}

	private boolean isOperatorOrHost(ServerPlayer player) {
		var server = getPlayerServer(player);
		return server.isSingleplayerOwner(player.nameAndId())
				|| server.getPlayerList().isOp(player.nameAndId());
	}

	private class EventListener implements ServerEventListener {

		@Override
		public void onServerStarting(MinecraftServer server) {
			loadModConfig(server);
		}

		@Override
		public void onServerReload(MinecraftServer server) {
			for (var player : server.getPlayerList().getPlayers()) {
				for (var category : getAllCategories()) {
					hideCategory(player, category);
				}
			}

			loadModConfig(server);

			for (var player : server.getPlayerList().getPlayers()) {
				updateAllCategories(player);
			}
		}

		@Override
		public void onPlayerJoin(ServerPlayer player) {
			PointsReward.cleanup(player);
			updateAllCategories(player);
		}

		@Override
		public void onPlayerLeave(ServerPlayer player) {
			for (var category : getAllCategories()) {
				resetRewards(player, category);
			}
		}

		@Override
		public void onCommandsRegister(CommandDispatcher<CommandSourceStack> dispatcher) {
			dispatcher.register(Commands.literal(SkillsAPI.MOD_ID)
					.then(CategoryCommand.create())
					.then(SkillsCommand.create())
					.then(PointsCommand.create())
					.then(ExperienceCommand.create())
					.then(LevelCommand.create())
					.then(OpenCommand.create())
			);
		}
	}
}
