package net.puffish.skillsmod.client.config.skill;

import net.minecraft.network.chat.Component;
import net.puffish.skillsmod.client.config.ClientFrameConfig;
import net.puffish.skillsmod.client.config.ClientIconConfig;

public record ClientSkillDefinitionConfig(
		String id,
		Component title,
		Component description,
		Component extraDescription,
		ClientIconConfig icon,
		ClientFrameConfig frame,
		float size,
		int cost,
		int requiredSkills,
		int requiredPoints,
		int requiredSpentPoints,
		int requiredExclusions
) { }
