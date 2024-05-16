package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class ClaimAllRewardsMessage implements CustomPacketPayload {
	public static final Type<ClaimAllRewardsMessage> TYPE = new Type<>(FTBQuestsAPI.rl("claim_all_rewards_message"));

	public static final ClaimAllRewardsMessage INSTANCE = new ClaimAllRewardsMessage();

	public static final StreamCodec<FriendlyByteBuf, ClaimAllRewardsMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<ClaimAllRewardsMessage> type() {
		return TYPE;
	}

	public static void handle(ClaimAllRewardsMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			ServerPlayer player = (ServerPlayer) context.getPlayer();
			TeamData data = TeamData.get(player);

			ServerQuestFile.INSTANCE.forAllQuests(quest -> {
				if (data.isCompleted(quest)) {
					quest.getRewards().stream()
							.filter(reward -> !reward.getExcludeFromClaimAll())
							.forEach(reward -> data.claimReward(player, reward, true));
				}
			});
		});
	}
}