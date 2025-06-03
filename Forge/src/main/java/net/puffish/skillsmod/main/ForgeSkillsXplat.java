package net.puffish.skillsmod.main;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.puffish.skillsmod.SkillsXplat;

public class ForgeSkillsXplat implements SkillsXplat {
  @Override
  public boolean isFakePlayer(ServerPlayerEntity player) {
    return player instanceof FakePlayer;
  }
}
