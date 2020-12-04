package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * @author LatvianModder
 */
public class LootCrateCategory implements IRecipeCategory<LootCrateWrapper>
{
	public static final ResourceLocation UID = new ResourceLocation(FTBQuests.MOD_ID, "lootcrates");

	public static final int ITEMSX = 10;
	public static final int ITEMSY = 5;
	public static final int ITEMS = ITEMSX * ITEMSY;

	private final IDrawable background;
	private final IDrawable icon;

	public LootCrateCategory(IGuiHelper guiHelper)
	{
		background = guiHelper.createBlankDrawable(ITEMSX * 18, ITEMSY * 18 + 36);
		icon = new IDrawable()
		{
			@Override
			public int getWidth()
			{
				return 16;
			}

			@Override
			public int getHeight()
			{
				return 16;
			}

			@Override
			public void draw(MatrixStack matrixStack, int xOffset, int yOffset)
			{
				matrixStack.push();
				matrixStack.translate(0, 0, 100);

				if (!LootCrateRegistry.INSTANCE.list.isEmpty())
				{
					GuiHelper.drawItem(matrixStack, LootCrateRegistry.INSTANCE.list.get((int) ((System.currentTimeMillis() / 1000L) % LootCrateRegistry.INSTANCE.list.size())).itemStack, xOffset, yOffset, 1F, 1F, true, null);
				}
				else
				{
					GuiHelper.drawItem(matrixStack, new ItemStack(FTBQuestsItems.LOOTCRATE), xOffset, yOffset, 1F, 1F, true, null);
				}

				matrixStack.pop();
			}
		};
	}

	@Override
	public ResourceLocation getUid()
	{
		return UID;
	}

	@Override
	public Class<LootCrateWrapper> getRecipeClass()
	{
		return LootCrateWrapper.class;
	}

	@Override
	public String getTitle()
	{
		return I18n.format("jei.ftbquests.lootcrates");
	}

	@Override
	public IDrawable getBackground()
	{
		return background;
	}

	@Override
	public IDrawable getIcon()
	{
		return icon;
	}

	@Override
	public void setIngredients(LootCrateWrapper wrapper, IIngredients iIngredients)
	{
		//FIXME
	}

	@Override
	public void setRecipe(IRecipeLayout layout, LootCrateWrapper entry, IIngredients ingredients)
	{
		IGuiItemStackGroup stacks = layout.getItemStacks();
		stacks.addTooltipCallback(entry);

		for (int slot = 0; slot < Math.min(ITEMS, entry.items.size()); slot++)
		{
			stacks.init(slot + 1, false, (slot % ITEMSX) * 18, (slot / ITEMSX) * 18 + 36);
		}

		stacks.set(ingredients);
	}
}