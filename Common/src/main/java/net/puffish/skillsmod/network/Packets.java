package net.puffish.skillsmod.network;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;

public class Packets {
	public static final Identifier SHOW_CATEGORY = SkillsMod.createIdentifier("show_category");
	public static final Identifier HIDE_CATEGORY = SkillsMod.createIdentifier("hide_category");
	public static final Identifier NEW_POINT = SkillsMod.createIdentifier("new_point");
	public static final Identifier SKILL_UPDATE = SkillsMod.createIdentifier("skill_update");
	public static final Identifier POINTS_UPDATE = SkillsMod.createIdentifier("points_update");
	public static final Identifier EXPERIENCE_UPDATE = SkillsMod.createIdentifier("experience_update");
	public static final Identifier SKILL_CLICK = SkillsMod.createIdentifier("skill_click");
	public static final Identifier SHOW_TOAST = SkillsMod.createIdentifier("show_toast");
	public static final Identifier OPEN_SCREEN = SkillsMod.createIdentifier("open_screen");
}
