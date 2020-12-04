package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftbquests.gui.GuiSelectQuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigCallback;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigValue;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ConfigQuestObject<T extends QuestObjectBase> extends ConfigValue<T>
{
	public final Predicate<QuestObjectBase> predicate;

	public ConfigQuestObject(Predicate<QuestObjectBase> t)
	{
		predicate = t;
	}

	@Override
	public ITextComponent getStringForGUI(@Nullable QuestObjectBase value)
	{
		if (value == null)
		{
			return StringTextComponent.EMPTY;
		}

		return value.getTitle();
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback)
	{
		if (getCanEdit())
		{
			new GuiSelectQuestObject<>(this, callback).openGui();
		}
	}

	@Override
	public void addInfo(TooltipList list)
	{
		if (value != null)
		{
			list.add(info("ID", value));
		}
	}
}