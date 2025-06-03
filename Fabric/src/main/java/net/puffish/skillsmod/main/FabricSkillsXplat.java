package net.puffish.skillsmod.main;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.SkillsXplat;

public class FabricSkillsXplat implements SkillsXplat {
  @Override
  public boolean isFakePlayer(ServerPlayerEntity player) {
    return player instanceof FakePlayer;
  }
}
