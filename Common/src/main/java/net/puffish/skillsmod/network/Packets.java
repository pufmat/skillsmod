package net.puffish.skillsmod.network;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;

public class Packets {
	public static final Identifier SHOW_CATEGORY = SkillsMod.createIdentifier("show_category");
	public static final Identifier HIDE_CATEGORY = SkillsMod.createIdentifier("hide_category");
	public static final Identifier SKILL_UNLOCK_PACKET = SkillsMod.createIdentifier("skill_unlock");
	public static final Identifier POINTS_UPDATE_PACKET = SkillsMod.createIdentifier("points_update");
	public static final Identifier EXPERIENCE_UPDATE_PACKET = SkillsMod.createIdentifier("experience_update");
	public static final Identifier SKILL_CLICK_PACKET = SkillsMod.createIdentifier("skill_click");
	public static final Identifier INVALID_CONFIG = SkillsMod.createIdentifier("invalid_config");
}
