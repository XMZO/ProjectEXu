package me.shepherd23333.projectex.integration.jei;

import me.shepherd23333.projecte.utils.EMCFormat;
import me.shepherd23333.projectex.tile.AlchemyTableRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;

import java.math.BigInteger;

/**
 * @author LatvianModder
 */
public class AlchemyTableWrapper implements IRecipeWrapper {
    public final AlchemyTableRecipe recipe;

    public AlchemyTableWrapper(AlchemyTableRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.input);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
    }

    @Override
    public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        BigInteger emc = recipe.getTotalCost();
        String s = EMCFormat.format(emc) + " EMC";
        mc.fontRenderer.drawString(s, (recipeWidth - mc.fontRenderer.getStringWidth(s)) / 2, 5, 0xFF222222);
    }
}