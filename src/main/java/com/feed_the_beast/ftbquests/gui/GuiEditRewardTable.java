package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigDouble;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetVerticalSpace;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiEditRewardTable extends GuiButtonListBase
{
	private class ButtonRewardTableSettings extends SimpleTextButton
	{
		private ButtonRewardTableSettings(Panel panel)
		{
			super(panel, new TranslationTextComponent("gui.settings"), GuiIcons.SETTINGS);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
			rewardTable.getConfig(rewardTable.createSubGroup(group));
			group.savedCallback = accepted -> run();
			new GuiEditConfig(group).openGui();
		}
	}

	private class ButtonSaveRewardTable extends SimpleTextButton
	{
		private ButtonSaveRewardTable(Panel panel)
		{
			super(panel, new TranslationTextComponent("gui.accept"), GuiIcons.ACCEPT);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			closeGui();
			CompoundNBT nbt = new CompoundNBT();
			rewardTable.writeData(nbt);
			originalTable.readData(nbt);
			callback.run();
		}
	}

	private class ButtonAddWeightedReward extends SimpleTextButton
	{
		private ButtonAddWeightedReward(Panel panel)
		{
			super(panel, new TranslationTextComponent("gui.add"), GuiIcons.ADD);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			for (RewardType type : RewardType.getRegistry())
			{
				if (!type.getExcludeFromListRewards())
				{
					contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
						playClickSound();
						type.getGuiProvider().openCreationGui(this, rewardTable.fakeQuest, reward -> {
							rewardTable.rewards.add(new WeightedReward(reward, 1));
							openGui();
						});
					}));
				}
			}

			getGui().openContextMenu(contextMenu);
		}
	}

	private class ButtonWeightedReward extends SimpleTextButton
	{
		private final WeightedReward reward;

		private ButtonWeightedReward(Panel panel, WeightedReward r)
		{
			super(panel, r.reward.getTitle(), r.reward.getIcon());
			reward = r;
		}

		@Override
		public void addMouseOverText(TooltipList list)
		{
			super.addMouseOverText(list);
			reward.reward.addMouseOverText(list);
			list.add(new TranslationTextComponent("ftbquests.reward_table.weight").appendString(": " + reward.weight).append(new StringTextComponent(" [" + WeightedReward.chanceString(reward.weight, rewardTable.getTotalWeight(true)) + "]").mergeStyle(TextFormatting.DARK_GRAY)));
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(new TranslationTextComponent("selectServer.edit"), GuiIcons.SETTINGS, () -> {
				ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
				reward.reward.getConfig(reward.reward.createSubGroup(group));
				group.savedCallback = accepted -> run();
				new GuiEditConfig(group).openGui();
			}));

			contextMenu.add(new ContextMenuItem(new TranslationTextComponent("ftbquests.reward_table.set_weight"), GuiIcons.SETTINGS, () -> {
				ConfigDouble c = new ConfigDouble(0D, Double.POSITIVE_INFINITY);
				GuiEditConfigFromString.open(c, (double) reward.weight, 1D, accepted -> {
					if (accepted)
					{
						reward.weight = c.value.intValue();

						if (c.value < 1D)
						{
							for (WeightedReward reward : rewardTable.rewards)
							{
								reward.weight = (int) (reward.weight / c.value);
							}

							reward.weight = 1;
						}
					}

					run();
				});
			}));

			contextMenu.add(new ContextMenuItem(new TranslationTextComponent("selectServer.delete"), GuiIcons.REMOVE, () -> {
				rewardTable.rewards.remove(reward);
				GuiEditRewardTable.this.refreshWidgets();
			}).setYesNo(new TranslationTextComponent("delete_item", reward.reward.getTitle())));
			GuiEditRewardTable.this.openContextMenu(contextMenu);
		}

		@Override
		@Nullable
		public Object getIngredientUnderMouse()
		{
			return reward.reward.getIngredient();
		}
	}

	private final RewardTable originalTable;
	private final RewardTable rewardTable;
	private final Runnable callback;

	public GuiEditRewardTable(RewardTable r, Runnable c)
	{
		originalTable = r;
		rewardTable = new RewardTable(originalTable.file);
		CompoundNBT nbt = new CompoundNBT();
		originalTable.writeData(nbt);
		rewardTable.readData(nbt);
		callback = c;
		setTitle(new TranslationTextComponent("ftbquests.reward_table"));
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel)
	{
		panel.add(new ButtonRewardTableSettings(panel));
		panel.add(new ButtonSaveRewardTable(panel));
		panel.add(new ButtonAddWeightedReward(panel));
		panel.add(new WidgetVerticalSpace(panel, 1));

		for (WeightedReward r : rewardTable.rewards)
		{
			panel.add(new ButtonWeightedReward(panel, r));
		}
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}
}