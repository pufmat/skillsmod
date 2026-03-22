package net.puffish.skillsmod.server.setup;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.commands.arguments.SkillArgumentType;

public class SkillsArgumentTypes {
	public static void register(ServerRegistrar registrar) {
		registrar.registerArgumentType(
				SkillsMod.createIdentifier("category"),
				CategoryArgumentType.class,
				new CategoryArgumentType.Info()
		);
		registrar.registerArgumentType(
				SkillsMod.createIdentifier("skill"),
				SkillArgumentType.class,
				new SkillArgumentType.Info()
		);
	}
}
