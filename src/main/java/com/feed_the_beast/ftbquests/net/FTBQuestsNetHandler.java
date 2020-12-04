package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Function;

public class FTBQuestsNetHandler
{
	public static SimpleChannel MAIN;
	private static final String GENERAL_VERSION = "1";
	private static int id = 0;

	private static <T extends MessageBase> void register(Class<T> c, Function<PacketBuffer, T> s)
	{
		MAIN.registerMessage(++id, c, MessageBase::write, s, MessageBase::handle);
	}

	public static void init()
	{
		MAIN = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation(FTBQuests.MOD_ID, "main"))
				.clientAcceptedVersions(GENERAL_VERSION::equals)
				.serverAcceptedVersions(GENERAL_VERSION::equals)
				.networkProtocolVersion(() -> GENERAL_VERSION)
				.simpleChannel();

		id = 0;

		// Game
		register(MessageSyncQuests.class, MessageSyncQuests::new);
		register(MessageUpdateTaskProgress.class, MessageUpdateTaskProgress::new);
		register(MessageSubmitTask.class, MessageSubmitTask::new);
		register(MessageClaimReward.class, MessageClaimReward::new);
		register(MessageClaimRewardResponse.class, MessageClaimRewardResponse::new);
		register(MessageSyncEditingMode.class, MessageSyncEditingMode::new);
		register(MessageGetEmergencyItems.class, MessageGetEmergencyItems::new);
		register(MessageCreatePlayerData.class, MessageCreatePlayerData::new);
		register(MessageClaimAllRewards.class, MessageClaimAllRewards::new);
		register(MessageClaimChoiceReward.class, MessageClaimChoiceReward::new);
		register(MessageDisplayCompletionToast.class, MessageDisplayCompletionToast::new);
		register(MessageDisplayRewardToast.class, MessageDisplayRewardToast::new);
		register(MessageDisplayItemRewardToast.class, MessageDisplayItemRewardToast::new);
		register(MessageTogglePinned.class, MessageTogglePinned::new);
		register(MessageTogglePinnedResponse.class, MessageTogglePinnedResponse::new);
		register(MessageUpdatePlayerData.class, MessageUpdatePlayerData::new);

		id = 100;

		// Editing
		register(MessageChangeProgress.class, MessageChangeProgress::new);
		register(MessageChangeProgressResponse.class, MessageChangeProgressResponse::new);
		register(MessageCreateObject.class, MessageCreateObject::new);
		register(MessageCreateObjectResponse.class, MessageCreateObjectResponse::new);
		register(MessageCreateTaskAt.class, MessageCreateTaskAt::new);
		register(MessageDeleteObject.class, MessageDeleteObject::new);
		register(MessageDeleteObjectResponse.class, MessageDeleteObjectResponse::new);
		register(MessageEditObject.class, MessageEditObject::new);
		register(MessageEditObjectResponse.class, MessageEditObjectResponse::new);
		register(MessageMoveChapter.class, MessageMoveChapter::new);
		register(MessageMoveChapterResponse.class, MessageMoveChapterResponse::new);
		register(MessageMoveQuest.class, MessageMoveQuest::new);
		register(MessageMoveQuestResponse.class, MessageMoveQuestResponse::new);
	}

	public static void writeItemType(PacketBuffer buffer, ItemStack stack)
	{
		if (stack.isEmpty())
		{
			buffer.writeVarInt(-1);
		}
		else
		{
			buffer.writeVarInt(Item.getIdFromItem(stack.getItem()));
			buffer.writeCompoundTag(stack.getTag());
			buffer.writeCompoundTag((CompoundNBT) stack.serializeNBT().get("ForgeCaps"));
		}
	}

	public static ItemStack readItemType(PacketBuffer buffer)
	{
		int id = buffer.readVarInt();

		if (id == -1)
		{
			return ItemStack.EMPTY;
		}
		else
		{
			CompoundNBT tag = buffer.readCompoundTag();
			CompoundNBT caps = buffer.readCompoundTag();
			ItemStack item = new ItemStack(Item.getItemById(id), 1, caps);
			item.setTag(tag);
			return item;
		}
	}
}