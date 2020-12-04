package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.net.MessageDisplayItemRewardToast;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.widget.WrappedIngredient;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ItemReward extends Reward
{
	public ItemStack item;
	public int count;
	public int randomBonus;
	public boolean onlyOne;

	public ItemReward(Quest quest, ItemStack is)
	{
		super(quest);
		item = is;
		count = 1;
		randomBonus = 0;
		onlyOne = false;
	}

	public ItemReward(Quest quest)
	{
		this(quest, new ItemStack(Items.APPLE));
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.ITEM;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		NBTUtils.write(nbt, "item", item);

		if (count > 1)
		{
			nbt.putInt("count", count);
		}

		if (randomBonus > 0)
		{
			nbt.putInt("random_bonus", randomBonus);
		}

		if (onlyOne)
		{
			nbt.putBoolean("only_one", true);
		}
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		item = NBTUtils.read(nbt, "item");

		count = nbt.getInt("count");

		if (count == 0)
		{
			count = item.getCount();
			item.setCount(1);
		}

		randomBonus = nbt.getInt("random_bonus");
		onlyOne = nbt.getBoolean("only_one");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		FTBQuestsNetHandler.writeItemType(buffer, item);
		buffer.writeVarInt(count);
		buffer.writeVarInt(randomBonus);
		buffer.writeBoolean(onlyOne);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		item = FTBQuestsNetHandler.readItemType(buffer);
		count = buffer.readVarInt();
		randomBonus = buffer.readVarInt();
		onlyOne = buffer.readBoolean();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addItemStack("item", item, v -> item = v, ItemStack.EMPTY, true, false).setNameKey("ftbquests.reward.ftbquests.item");
		config.addInt("count", count, v -> count = v, 1, 1, 8192);
		config.addInt("random_bonus", randomBonus, v -> randomBonus = v, 0, 0, 8192).setNameKey("ftbquests.reward.random_bonus");
		config.addBool("only_one", onlyOne, v -> onlyOne = v, false);
	}

	@Override
	public void claim(ServerPlayerEntity player, boolean notify)
	{
		if (onlyOne && player.inventory.hasItemStack(item))
		{
			return;
		}

		int size = count + player.world.rand.nextInt(randomBonus + 1);

		while (size > 0)
		{
			int s = Math.min(size, item.getMaxStackSize());
			ItemHandlerHelper.giveItemToPlayer(player, ItemHandlerHelper.copyStackWithSize(item, s));
			size -= s;
		}

		if (notify)
		{
			new MessageDisplayItemRewardToast(item, size).sendTo(player);
		}
	}

	@Override
	public boolean automatedClaimPre(TileEntity tileEntity, List<ItemStack> items, Random random, UUID playerId, @Nullable ServerPlayerEntity player)
	{
		int size = count + random.nextInt(randomBonus + 1);

		while (size > 0)
		{
			int s = Math.min(size, item.getMaxStackSize());
			items.add(ItemHandlerHelper.copyStackWithSize(item, s));
			size -= s;
		}

		return true;
	}

	@Override
	public void automatedClaimPost(TileEntity tileEntity, UUID playerId, @Nullable ServerPlayerEntity player)
	{
	}

	@Override
	public Icon getAltIcon()
	{
		if (item.isEmpty())
		{
			return super.getAltIcon();
		}

		return ItemIcon.getItemIcon(ItemHandlerHelper.copyStackWithSize(item, 1));
	}

	@Override
	public IFormattableTextComponent getAltTitle()
	{
		return new StringTextComponent((count > 1 ? (randomBonus > 0 ? (count + "-" + (count + randomBonus) + "x ") : (count + "x ")) : "")).append(item.getDisplayName());
	}

	@Override
	public boolean addTitleInMouseOverText()
	{
		return !getTitle().equals(getAltTitle());
	}

	@Nullable
	@Override
	public Object getIngredient()
	{
		return new WrappedIngredient(item).tooltip();
	}

	@Override
	public String getButtonText()
	{
		if (randomBonus > 0)
		{
			return count + "-" + (count + randomBonus);
		}

		return Integer.toString(count);
	}
}