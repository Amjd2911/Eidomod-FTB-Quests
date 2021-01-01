package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.gui.quests.GuiQuests;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.WrappedIngredient;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class Task extends QuestObject
{
	public final Quest quest;

	public Task(Quest q)
	{
		quest = q;
	}

	@Override
	public final String toString()
	{
		return quest.chapter.filename + ":" + quest.getCodeString() + ":T:" + getCodeString();
	}

	@Override
	public final QuestObjectType getObjectType()
	{
		return QuestObjectType.TASK;
	}

	@Override
	public final QuestFile getQuestFile()
	{
		return quest.chapter.file;
	}

	@Override
	public final Chapter getQuestChapter()
	{
		return quest.chapter;
	}

	@Override
	public final int getParentID()
	{
		return quest.id;
	}

	public abstract TaskType getType();

	public abstract TaskData createData(PlayerData data);

	@Override
	public final int getRelativeProgressFromChildren(PlayerData data)
	{
		return data.getTaskData(this).getRelativeProgress();
	}

	@Override
	public final void onCompleted(PlayerData data, List<ServerPlayerEntity> onlineMembers, List<ServerPlayerEntity> notifiedPlayers)
	{
		super.onCompleted(data, onlineMembers, notifiedPlayers);
		MinecraftForge.EVENT_BUS.post(new ObjectCompletedEvent.TaskEvent(data, this, onlineMembers, notifiedPlayers));
		boolean questComplete = data.isComplete(quest);

		if (quest.tasks.size() > 1 && !questComplete && !disableToast)
		{
			new MessageDisplayCompletionToast(id).sendTo(notifiedPlayers);
		}

		if (questComplete)
		{
			quest.onCompleted(data, onlineMembers, notifiedPlayers);
		}
	}

	public long getMaxProgress()
	{
		return 1L;
	}

	public String getMaxProgressString()
	{
		return StringUtils.formatDouble(getMaxProgress(), true);
	}

	@Override
	public final void changeProgress(PlayerData data, ChangeProgress type)
	{
		data.getTaskData(this).setProgress(type.reset ? 0L : getMaxProgress());
	}

	@Override
	public final void deleteSelf()
	{
		quest.tasks.remove(this);

		for (PlayerData data : quest.chapter.file.getAllData())
		{
			data.removeTaskData(this);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren()
	{
		for (PlayerData data : quest.chapter.file.getAllData())
		{
			data.removeTaskData(this);
		}

		super.deleteChildren();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void editedFromGUI()
	{
		GuiQuests gui = ClientUtils.getCurrentGuiAs(GuiQuests.class);

		if (gui != null)
		{
			gui.questPanel.refreshWidgets();
			gui.viewQuestPanel.refreshWidgets();
		}
	}

	@Override
	public final void onCreated()
	{
		quest.tasks.add(this);

		for (PlayerData data : quest.chapter.file.getAllData())
		{
			data.createTaskData(this, true);
		}

		if (this instanceof CustomTask)
		{
			MinecraftForge.EVENT_BUS.post(new CustomTaskEvent((CustomTask) this));
		}
	}

	@Override
	public Icon getAltIcon()
	{
		return getType().getIcon();
	}

	@Override
	public IFormattableTextComponent getAltTitle()
	{
		return getType().getDisplayName();
	}

	@Override
	public final ConfigGroup createSubGroup(ConfigGroup group)
	{
		TaskType type = getType();
		return group.getGroup(getObjectType().id).getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
	}

	public void drawGUI(@Nullable TaskData data, MatrixStack matrixStack, int x, int y, int w, int h)
	{
		getIcon().draw(matrixStack, x, y, w, h);
	}

	public boolean canInsertItem()
	{
		return false;
	}

	public boolean consumesResources()
	{
		return canInsertItem();
	}

	public boolean hideProgressNumbers()
	{
		return getMaxProgress() <= 1L;
	}

	@OnlyIn(Dist.CLIENT)
	public void addMouseOverText(TooltipList list, @Nullable TaskData data)
	{
		if (consumesResources())
		{
			list.blankLine();
			list.add(new TranslationTextComponent("ftbquests.task.click_to_submit").mergeStyle(TextFormatting.YELLOW, TextFormatting.UNDERLINE));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public boolean addTitleInMouseOverText()
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public void onButtonClicked(Button button, boolean canClick)
	{
		if (canClick && autoSubmitOnPlayerTick() <= 0)
		{
			button.playClickSound();
			new MessageSubmitTask(id).sendToServer();
		}
	}

	public boolean submitItemsOnInventoryChange()
	{
		return false;
	}

	@Nullable
	public Object getIngredient()
	{
		if (addTitleInMouseOverText())
		{
			return getIcon().getIngredient();
		}

		return new WrappedIngredient(getIcon().getIngredient()).tooltip();
	}

	@Override
	public final int refreshJEI()
	{
		return FTBQuestsJEIHelper.QUESTS;
	}

	@OnlyIn(Dist.CLIENT)
	public IFormattableTextComponent getButtonText()
	{
		return getMaxProgress() > 1L || consumesResources() ? new StringTextComponent(getMaxProgressString()) : (IFormattableTextComponent) StringTextComponent.EMPTY;
	}

	public int autoSubmitOnPlayerTick()
	{
		return 0;
	}

	@Override
	public final boolean cacheProgress()
	{
		return false;
	}
}