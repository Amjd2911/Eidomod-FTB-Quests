package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.net.MessageTogglePinned;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author LatvianModder
 */
public class ButtonAutopin extends ButtonTab
{
	public ButtonAutopin(Panel panel)
	{
		super(panel, new TranslationTextComponent(((GuiQuests) panel.getGui()).file.self.getAutoPin() ? "ftbquests.gui.autopin.on" : "ftbquests.gui.autopin.off"), ((GuiQuests) panel.getGui()).file.self.getAutoPin() ? ThemeProperties.PIN_ICON_ON.get() : ThemeProperties.PIN_ICON_OFF.get());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		new MessageTogglePinned(1).sendToServer();
	}
}