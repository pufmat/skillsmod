package net.puffish.skillsmod.server.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ServerEventListener {
	void onServerStarting(MinecraftServer server);

	void onServerReload(MinecraftServer server);

	void onPlayerJoin(ServerPlayerEntity player);

	void onCommandsRegister(CommandDispatcher<ServerCommandSource> dispatcher);
}
