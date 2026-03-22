package net.puffish.skillsmod.server.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface ServerEventListener {
	void onServerStarting(MinecraftServer server);

	void onServerReload(MinecraftServer server);

	void onPlayerJoin(ServerPlayer player);

	void onPlayerLeave(ServerPlayer player);

	void onCommandsRegister(CommandDispatcher<CommandSourceStack> dispatcher);
}
