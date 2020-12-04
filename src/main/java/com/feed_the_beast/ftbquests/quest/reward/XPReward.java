package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.net.MessageDisplayRewardToast;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class XPReward extends Reward
{
	public int xp;

	public XPReward(Quest quest, int x)
	{
		super(quest);
		xp = x;
	}

	public XPReward(Quest quest)
	{
		this(quest, 100);
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.XP;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putInt("xp", xp);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		xp = nbt.getInt("xp");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeVarInt(xp);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		xp = buffer.readVarInt();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("xp", xp, v -> xp = v, 100, 1, Integer.MAX_VALUE).setNameKey("ftbquests.reward.ftbquests.xp");
	}

	@Override
	public void claim(ServerPlayerEntity player, boolean notify)
	{
		player.giveExperiencePoints(xp);

		if (notify)
		{
			new MessageDisplayRewardToast(id, new TranslationTextComponent("ftbquests.reward.ftbquests.xp").appendString(": ").append(new StringTextComponent("+" + xp).mergeStyle(TextFormatting.GREEN)), Icon.EMPTY).sendTo(player);
		}
	}

	@Override
	public IFormattableTextComponent getAltTitle()
	{
		return new TranslationTextComponent("ftbquests.reward.ftbquests.xp").appendString(": ").append(new StringTextComponent("+" + xp).mergeStyle(TextFormatting.GREEN));
	}

	@Override
	public String getButtonText()
	{
		return "+" + xp;
	}
}