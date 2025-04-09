package net.puffish.skillsmod.server.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerData extends PersistentState {
	private final Map<UUID, PlayerData> players = new HashMap<>();

	private ServerData() {

	}

	private static ServerData read(NbtCompound tag) {
		var playersData = new ServerData();

		var playersNbt = tag.getCompoundOrEmpty("players");
		playersNbt.getKeys().forEach(key -> playersData.players.put(
				UUID.fromString(key),
				PlayerData.read(playersNbt.getCompoundOrEmpty(key))
		));

		return playersData;
	}

	private NbtCompound writeNbt(NbtCompound nbt) {
		var playersNbt = new NbtCompound();
		for (var entry : players.entrySet()) {
			playersNbt.put(
					entry.getKey().toString(),
					entry.getValue().writeNbt(new NbtCompound())
			);
		}
		nbt.put("players", playersNbt);

		return nbt;
	}

	public static PersistentStateType<ServerData> getPersistentStateType() {
		return new PersistentStateType<>(
				SkillsAPI.MOD_ID,
				context -> new ServerData(),
				context -> new Codec<>() {
					@Override
					public <T> DataResult<Pair<ServerData, T>> decode(DynamicOps<T> ops, T input) {
						return DataResult.success(Pair.of(ServerData.read((NbtCompound) input), input));
					}

					@Override
					@SuppressWarnings("unchecked")
					public <T> DataResult<T> encode(ServerData input, DynamicOps<T> ops, T prefix) {
						if (!(prefix instanceof NbtEnd)) {
							throw new RuntimeException();
						}
						return DataResult.success((T) input.writeNbt(new NbtCompound()));
					}
				},
				null
		);
	}

	public static ServerData getOrCreate(MinecraftServer server) {
		var persistentStateManager = server.getOverworld().getPersistentStateManager();

		return persistentStateManager.getOrCreate(getPersistentStateType());
	}

	public PlayerData getPlayerData(ServerPlayerEntity player) {
		return players.computeIfAbsent(player.getUuid(), uuid -> PlayerData.empty());
	}

	public void putPlayerData(ServerPlayerEntity player, PlayerData data) {
		players.put(player.getUuid(), data);
	}

	@Override
	public boolean isDirty() {
		return true;
	}
}
