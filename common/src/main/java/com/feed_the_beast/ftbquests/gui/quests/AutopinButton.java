package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.net.MessageTogglePinned;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class AutopinButton extends TabButton {
	public AutopinButton(Panel panel) {
		super(panel, new TranslatableComponent(((QuestScreen) panel.getGui()).file.self.getAutoPin() ? "ftbquests.gui.autopin.on" : "ftbquests.gui.autopin.off"), ((QuestScreen) panel.getGui()).file.self.getAutoPin() ? ThemeProperties.PIN_ICON_ON.get() : ThemeProperties.PIN_ICON_OFF.get());
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		new MessageTogglePinned(1).sendToServer();
	}
}