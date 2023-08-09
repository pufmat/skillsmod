package net.puffish.skillsmod.rewards.builtin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.rewards.Reward;
import net.puffish.skillsmod.rewards.RewardContext;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;

public class TagReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("tag");

	private final String tag;

	private TagReward(String tag) {
		this.tag = tag;
	}

	public static void register() {
		SkillsAPI.registerRewardWithData(
				ID,
				TagReward::create
		);
	}

	private static Result<TagReward, Failure> create(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(TagReward::create);
	}

	private static Result<TagReward, Failure> create(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optTag = rootObject.getString("tag")
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new TagReward(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public void update(ServerPlayerEntity player, RewardContext context) {
		if (context.count() > 0) {
			player.addScoreboardTag(tag);
		} else {
			player.removeScoreboardTag(tag);
		}
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
