package me.shepherd23333.projectex.tile;

import me.shepherd23333.projecte.api.ProjectEAPI;
import me.shepherd23333.projecte.utils.Constants;
import net.minecraft.item.ItemStack;

import java.math.BigInteger;

/**
 * @author LatvianModder
 */
public class AlchemyTableRecipe {
    public ItemStack input = ItemStack.EMPTY;
    public ItemStack output = ItemStack.EMPTY;
    public BigInteger emcOverride = BigInteger.ZERO;
    public int progressOverride = 0;

    public BigInteger getTotalCost() {
        if (emcOverride.compareTo(BigInteger.ZERO) > 0) {
            return emcOverride;
        }

        return ProjectEAPI.getEMCProxy().getValue(input)
                .add(ProjectEAPI.getEMCProxy().getValue(output))
                .multiply(BigInteger.valueOf(3))
                .max(Constants.cons1);
    }

    public int getTotalProgress() {
        return progressOverride > 0 ? progressOverride : 200;
    }
}