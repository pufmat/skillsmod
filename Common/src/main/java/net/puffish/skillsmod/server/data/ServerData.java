package net.puffish.skillsmod.server.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerData extends SavedData {
	private final Map<UUID, PlayerData> players = new HashMap<>();

	private ServerData() {

	}

	private static ServerData read(CompoundTag tag) {
		var playersData = new ServerData();

		var playersNbt = tag.getCompoundOrEmpty("players");
		playersNbt.keySet().forEach(key -> playersData.players.put(
				UUID.fromString(key),
				PlayerData.read(playersNbt.getCompoundOrEmpty(key))
		));

		return playersData;
	}

	private CompoundTag writeNbt(CompoundTag nbt) {
		var playersNbt = new CompoundTag();
		for (var entry : players.entrySet()) {
			playersNbt.put(
					entry.getKey().toString(),
					entry.getValue().writeNbt(new CompoundTag())
			);
		}
		nbt.put("players", playersNbt);

		return nbt;
	}

	public static SavedDataType<ServerData> getPersistentStateType() {
		return new SavedDataType<>(
				SkillsAPI.MOD_ID,
				ServerData::new,
				new Codec<>() {
					@Override
					public <T> DataResult<Pair<ServerData, T>> decode(DynamicOps<T> ops, T input) {
						return DataResult.success(Pair.of(ServerData.read((CompoundTag) input), input));
					}

					@Override
					@SuppressWarnings("unchecked")
					public <T> DataResult<T> encode(ServerData input, DynamicOps<T> ops, T prefix) {
						if (!(prefix instanceof EndTag)) {
							throw new RuntimeException();
						}
						return DataResult.success((T) input.writeNbt(new CompoundTag()));
					}
				},
				null
		);
	}

	public static ServerData getOrCreate(MinecraftServer server) {
		var persistentStateManager = server.overworld().getDataStorage();

		return persistentStateManager.computeIfAbsent(getPersistentStateType());
	}

	public PlayerData getPlayerData(ServerPlayer player) {
		return players.computeIfAbsent(player.getUUID(), uuid -> PlayerData.empty());
	}

	public void putPlayerData(ServerPlayer player, PlayerData data) {
		players.put(player.getUUID(), data);
	}

	@Override
	public boolean isDirty() {
		return true;
	}
}
