package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import com.mojang.blaze3d.matrix.MatrixStack;

/**
 * @author LatvianModder
 */
public class PanelChapterHover extends Panel
{
	public final GuiQuests treeGui;
	public ButtonChapter chapter = null;
	public int type = -1;

	public PanelChapterHover(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuests) panel.getGui();
		setPosAndSize(-1, -1, 0, 0);
	}

	@Override
	public void addWidgets()
	{
		if (chapter != null)
		{
			type = 0;

			for (Chapter c : treeGui.file.chapters)
			{
				if (c.group == chapter.chapter && (treeGui.file.canEdit() || c.isVisible(treeGui.file.self)))
				{
					if (widgets.isEmpty())
					{
						type = 1;
						add(new ButtonExpandedChapter(this, chapter.chapter));
					}

					ButtonExpandedChapter b = new ButtonExpandedChapter(this, c);
					b.setX(20);
					add(b);
				}
			}

			if (type == 0)
			{
				add(new ButtonExpandedChapter(this, chapter.chapter));
			}

			setHeight(align(WidgetLayout.VERTICAL));
			setWidth(Math.max(20, getContentWidth()));

			for (Widget widget : widgets)
			{
				widget.setWidth(width - widget.posX);
			}

			setX(chapter.getX());
		}
		else
		{
			type = -1;
			setPosAndSize(-1, -1, 0, 0);
		}
	}

	@Override
	public int getY()
	{
		if (chapter != null)
		{
			treeGui.chapterPanel.setOffset(true);
			int y = Math.min(chapter.getY() - 1, treeGui.height - height - 1);
			treeGui.chapterPanel.setOffset(false);
			return y;
		}

		return -1;
	}

	@Override
	public void alignWidgets()
	{
	}

	@Override
	public void drawBackground(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
	}

	@Override
	public void draw(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		if (type != -1)
		{
			matrixStack.push();
			matrixStack.translate(0, 0, 500);
			super.draw(matrixStack, theme, x, y, w, h);
			matrixStack.pop();
		}
	}

	@Override
	public void tick()
	{
		if (!isMouseOverAnyWidget())
		{
			ButtonChapter c = null;

			for (Widget widget : treeGui.chapterPanel.widgets)
			{
				if (widget instanceof ButtonChapter && widget.isMouseOver())
				{
					c = (ButtonChapter) widget;
				}
			}

			if (chapter != c)
			{
				chapter = c;
				refreshWidgets();
			}
		}

		super.tick();
	}
}