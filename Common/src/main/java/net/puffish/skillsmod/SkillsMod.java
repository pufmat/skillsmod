package net.puffish.skillsmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.puffish.skillsmod.commands.CategoryCommand;
import net.puffish.skillsmod.commands.ExperienceCommand;
import net.puffish.skillsmod.commands.PointsCommand;
import net.puffish.skillsmod.commands.SkillsCommand;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.config.ModConfig;
import net.puffish.skillsmod.experience.ExperienceSource;
import net.puffish.skillsmod.experience.builtin.TakeDamageExperienceSource;
import net.puffish.skillsmod.server.PlayerAttributes;
import net.puffish.skillsmod.config.experience.ExperienceSourceConfig;
import net.puffish.skillsmod.experience.builtin.CraftItemExperienceSource;
import net.puffish.skillsmod.experience.builtin.EatFoodExperienceSource;
import net.puffish.skillsmod.server.SkillsGameRules;
import net.puffish.skillsmod.json.JsonPath;
import net.puffish.skillsmod.rewards.builtin.CommandReward;
import net.puffish.skillsmod.rewards.builtin.ScoreboardReward;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.experience.builtin.MineBlockExperienceSource;
import net.puffish.skillsmod.experience.builtin.KillEntityExperienceSource;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.rewards.builtin.AttributeReward;
import net.puffish.skillsmod.server.data.PlayerData;
import net.puffish.skillsmod.server.event.ServerEventListener;
import net.puffish.skillsmod.server.event.ServerEventReceiver;
import net.puffish.skillsmod.server.setup.ServerGameRules;
import net.puffish.skillsmod.server.setup.ServerRegistrar;
import net.puffish.skillsmod.server.network.ServerPacketReceiver;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import net.puffish.skillsmod.server.data.ServerData;
import net.puffish.skillsmod.server.network.packets.in.SkillClickInPacket;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.server.network.packets.out.ExperienceUpdateOutPacket;
import net.puffish.skillsmod.server.network.packets.out.HideCategoryOutPacket;
import net.puffish.skillsmod.server.network.packets.out.InvalidConfigOutPacket;
import net.puffish.skillsmod.server.network.packets.out.PointsUpdateOutPacket;
import net.puffish.skillsmod.server.network.packets.out.ShowCategoryOutPacket;
import net.puffish.skillsmod.server.network.packets.out.SkillUnlockOutPacket;
import net.puffish.skillsmod.utils.ChangeListener;
import net.puffish.skillsmod.utils.PathUtils;
import net.puffish.skillsmod.utils.PrefixedLogger;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

	private ChangeListener<Optional<Map<String, CategoryConfig>>> categories = null;

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

		MineBlockExperienceSource.register();
		KillEntityExperienceSource.register();
		EatFoodExperienceSource.register();
		CraftItemExperienceSource.register();
		TakeDamageExperienceSource.register();
	}

	public static Identifier createIdentifier(String path) {
		return new Identifier(SkillsAPI.MOD_ID, path);
	}

	public static MutableText createTranslatable(String type, String path, Object... args) {
		return Text.translatable(Util.createTranslationKey(type, createIdentifier(path)), args);
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

	private void loadConfig(ConfigContext context) {
		if (!Files.exists(modConfigDir) || PathUtils.isDirectoryEmpty(modConfigDir)) {
			copyConfigFromJar();
		}

		var configFile = modConfigDir.resolve("config.json");

		PathUtils.createFileIfMissing(configFile);

		JsonElementWrapper.parseFile(configFile, JsonPath.fromPath(modConfigDir.relativize(configFile)))
				.andThen(ModConfig::parse)
				.andThen(modConfig -> readCategories(modConfig.getCategories(), context))
				.peek(map -> {
					logger.info("Configuration loaded successfully!");
					categories.set(Optional.of(map));
				}, error -> {
					logger.error("Configuration could not be loaded:" + System.lineSeparator() + error.getMessages().stream().collect(Collectors.joining(System.lineSeparator())));
					categories.set(Optional.empty());
				});
	}

	private Result<Map<String, CategoryConfig>, Error> readCategories(List<String> ids, ConfigContext context) {
		var errors = new ArrayList<Error>();

		var map = new HashMap<String, CategoryConfig>();

		var categoriesDir = modConfigDir.resolve("categories");

		for (var i = 0; i < ids.size(); i++) {
			var id = ids.get(i);
			readCategory(id, i, categoriesDir.resolve(id), context)
					.ifFailure(errors::add)
					.ifSuccess(category -> map.put(id, category));
		}

		if (errors.isEmpty()) {
			return Result.success(map);
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	private Result<CategoryConfig, Error> readCategory(String id, int index, Path categoryDir, ConfigContext context) {
		Path generalFile = categoryDir.resolve("category.json");
		Path definitionsFile = categoryDir.resolve("definitions.json");
		Path skillsFile = categoryDir.resolve("skills.json");
		Path connectionsFile = categoryDir.resolve("connections.json");
		Path experienceFile = categoryDir.resolve("experience.json");

		PathUtils.createFileIfMissing(generalFile);
		PathUtils.createFileIfMissing(definitionsFile);
		PathUtils.createFileIfMissing(skillsFile);
		PathUtils.createFileIfMissing(connectionsFile);
		PathUtils.createFileIfMissing(experienceFile);

		var errors = new ArrayList<Error>();

		var generalElement = JsonElementWrapper.parseFile(generalFile, JsonPath.fromPath(modConfigDir.relativize(generalFile)))
				.ifFailure(errors::add)
				.getSuccess();

		var definitionsElement = JsonElementWrapper.parseFile(definitionsFile, JsonPath.fromPath(modConfigDir.relativize(definitionsFile)))
				.ifFailure(errors::add)
				.getSuccess();

		var skillsElement = JsonElementWrapper.parseFile(skillsFile, JsonPath.fromPath(modConfigDir.relativize(skillsFile)))
				.ifFailure(errors::add)
				.getSuccess();

		var connectionsElement = JsonElementWrapper.parseFile(connectionsFile, JsonPath.fromPath(modConfigDir.relativize(connectionsFile)))
				.ifFailure(errors::add)
				.getSuccess();

		var experienceElement = JsonElementWrapper.parseFile(experienceFile, JsonPath.fromPath(modConfigDir.relativize(experienceFile)))
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return CategoryConfig.parse(
					id,
					index,
					generalElement.orElseThrow(),
					definitionsElement.orElseThrow(),
					skillsElement.orElseThrow(),
					connectionsElement.orElseThrow(),
					experienceElement.orElseThrow(),
					context
			);
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	private void onSkillClickPacket(ServerPlayerEntity player, SkillClickInPacket packet) {
		if (player.isSpectator()) {
			return;
		}
		tryUnlockSkill(player, packet.getCategoryId(), packet.getSkillId(), false);
	}

	public void unlockSkill(ServerPlayerEntity player, String categoryId, String skillId) {
		tryUnlockSkill(player, categoryId, skillId, true);
	}

	private void tryUnlockSkill(ServerPlayerEntity player, String categoryId, String skillId, boolean force) {
		getCategory(categoryId).ifPresent(category -> getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
			if (category.tryUnlockSkill(player, categoryData, skillId, force)) {
				packetSender.send(player, SkillUnlockOutPacket.write(categoryId, skillId));
				syncPoints(player, category, categoryData);
			}
		}));
	}

	public void resetSkills(ServerPlayerEntity player, String categoryId) {
		getCategory(categoryId).ifPresent(category -> getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
			categoryData.resetSkills();
			applyRewards(player, category, categoryData);
			syncCategory(player, category, categoryData);
		}));
	}

	public void eraseCategory(ServerPlayerEntity player, String categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var playerData = getPlayerData(player);
			playerData.removeCategoryData(category);

			syncCategory(player, category);
		});
	}

	public void unlockCategory(ServerPlayerEntity player, String categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var playerData = getPlayerData(player);
			playerData.unlockCategory(category);

			syncCategory(player, category);
		});
	}

	public void lockCategory(ServerPlayerEntity player, String categoryId) {
		getCategory(categoryId).ifPresent(category -> {
			var playerData = getPlayerData(player);
			playerData.lockCategory(category);

			syncCategory(player, category);
		});
	}

	public void addExperience(ServerPlayerEntity player, String categoryId, int amount) {
		getCategory(categoryId).ifPresent(category -> {
			if (!category.getExperience().isEnabled()) {
				return;
			}

			getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
				categoryData.addExperience(amount);

				syncExperience(player, category, categoryData);
				syncPoints(player, category, categoryData);
			});
		});
	}

	public void setExperience(ServerPlayerEntity player, String categoryId, int amount) {
		getCategory(categoryId).ifPresent(category -> {
			if (!category.getExperience().isEnabled()) {
				return;
			}

			getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
				categoryData.setExperience(amount);

				syncExperience(player, category, categoryData);
				syncPoints(player, category, categoryData);
			});
		});
	}

	public Optional<Integer> getExperience(ServerPlayerEntity player, String categoryId) {
		return getCategory(categoryId).flatMap(category -> {
			if (!category.getExperience().isEnabled()) {
				return Optional.empty();
			}

			return getCategoryDataIfUnlocked(player, category).map(CategoryData::getExperience);
		});
	}

	public void addExtraPoints(ServerPlayerEntity player, String categoryId, int count) {
		getCategory(categoryId).ifPresent(category -> getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
			categoryData.addExtraPoints(count);

			syncPoints(player, category, categoryData);
		}));
	}

	public void setExtraPoints(ServerPlayerEntity player, String categoryId, int count) {
		getCategory(categoryId).ifPresent(category -> getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
			categoryData.setExtraPoints(count);

			syncPoints(player, category, categoryData);
		}));
	}

	public Optional<Integer> getExtraPoints(ServerPlayerEntity player, String categoryId) {
		return getCategory(categoryId)
				.flatMap(category -> getCategoryDataIfUnlocked(player, category)
						.map(CategoryData::getExtraPoints)
				);
	}

	public void setPointsLeft(ServerPlayerEntity player, String categoryId, int count) {
		getCategory(categoryId).ifPresent(category -> {
			var categoryData = getPlayerData(player).getCategoryData(category);
			categoryData.setPointsLeft(count, category);

			syncPoints(player, category, categoryData);
		});
	}

	public Optional<Integer> getPointsLeft(ServerPlayerEntity player, String categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getCategoryData(category);
			return categoryData.getPointsLeft(category);
		});
	}

	public Collection<String> getUnlockedCategories(ServerPlayerEntity player) {
		var playerData = getPlayerData(player);

		return getAllCategories()
				.stream()
				.filter(playerData::isCategoryUnlocked)
				.map(CategoryConfig::getId)
				.toList();
	}

	public Collection<String> getCategories() {
		return getAllCategories()
				.stream()
				.map(CategoryConfig::getId)
				.toList();
	}

	public Optional<Collection<String>> getUnlockedSkills(ServerPlayerEntity player, String categoryId) {
		return getCategory(categoryId).map(category -> {
			var categoryData = getPlayerData(player).getCategoryData(category);
			return categoryData.getUnlockedSkillIds();
		});
	}

	public Optional<Collection<String>> getSkills(String categoryId) {
		return getCategory(categoryId).map(
				category -> category.getSkills()
						.getAll()
						.stream()
						.map(SkillConfig::getId)
						.toList()
		);
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
				categoryData.getPointsLeft(category),
				player.getWorld().getGameRules().getBoolean(SkillsGameRules.ANNOUNCE_NEW_POINTS)
		));
	}

	private void syncExperience(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData) {
		packetSender.send(player, ExperienceUpdateOutPacket.write(
				category.getId(),
				category.getExperience().getProgress(categoryData)
		));
	}

	public void refreshReward(ServerPlayerEntity player, Identifier type) {
		for (CategoryConfig category : getAllCategories()) {
			var categoryData = getPlayerData(player).getCategoryData(category);
			category.refreshReward(player, categoryData, type);
		}
	}

	public void visitExperienceSources(ServerPlayerEntity player, Function<ExperienceSource, Integer> function) {
		for (CategoryConfig category : getAllCategories()) {
			getCategoryDataIfUnlocked(player, category).ifPresent(categoryData -> {
				int amount = 0;

				for (ExperienceSourceConfig experienceSource : category.getExperience().getExperienceSources()) {
					amount += function.apply(experienceSource.getInstance());
				}

				if (amount != 0) {
					categoryData.addExperience(amount);

					syncExperience(player, category, categoryData);
					syncPoints(player, category, categoryData);
				}
			});
		}
	}

	private void applyRewards(ServerPlayerEntity player, CategoryConfig category, CategoryData categoryData) {
		category.applyRewards(player, categoryData);
	}

	private void resetRewards(ServerPlayerEntity player, CategoryConfig category) {
		category.resetRewards(player);
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

	private Optional<CategoryConfig> getCategory(String categoryId) {
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
			categories = new ChangeListener<>(old -> old.ifPresent(map -> {
				for (CategoryConfig category : map.values()) {
					category.dispose(server);
				}
			}), null);

			loadConfig(new ConfigContext(server));
		}

		@Override
		public void onServerReload(MinecraftServer server) {
			for (var player : server.getPlayerManager().getPlayerList()) {
				for (var category : getAllCategories()) {
					hideCategory(player, category);
				}
			}

			loadConfig(new ConfigContext(server));

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
