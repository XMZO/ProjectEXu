package me.shepherd23333.projectex.item;

import me.shepherd23333.projecte.api.item.IItemEmc;
import me.shepherd23333.projecte.gameObjs.items.ItemPE;
import me.shepherd23333.projecte.gameObjs.items.KleinStar;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author LatvianModder
 */
public class ItemMagnumStar extends Item implements IItemEmc {
    public static final BigInteger[] STAR_EMC = new BigInteger[12];

    static {
        BigInteger emc = BigInteger.valueOf(204800000L);

        for (int i = 0; i < STAR_EMC.length; i++) {
            STAR_EMC[i] = emc;
            emc = emc.multiply(BigInteger.valueOf(4));
        }
    }

    public final KleinStar.EnumKleinTier tier;

    public ItemMagnumStar(KleinStar.EnumKleinTier t) {
        tier = t;
        setMaxStackSize(1);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getStoredEmc(stack).compareTo(BigInteger.ZERO) > 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        BigInteger emc = getStoredEmc(stack);
        return emc.equals(BigInteger.ZERO) ? 1D :
                BigDecimal.ONE.subtract(new BigDecimal(emc).divide(new BigDecimal(getMaximumEmc(stack)))
                        .max(BigDecimal.ZERO)).doubleValue();
    }

    @Override
    public BigInteger addEmc(ItemStack stack, BigInteger toAdd) {
        BigInteger add = getMaximumEmc(stack).subtract(getStoredEmc(stack)).min(toAdd);
        ItemPE.addEmcToStack(stack, add);
        return add;
    }

    @Override
    public BigInteger extractEmc(ItemStack stack, BigInteger toRemove) {
        BigInteger sub = getStoredEmc(stack).min(toRemove);
        ItemPE.removeEmc(stack, sub);
        return sub;
    }

    @Override
    public BigInteger getStoredEmc(ItemStack stack) {
        return ItemPE.getEmc(stack);
    }

    @Override
    public BigInteger getMaximumEmc(ItemStack stack) {
        return STAR_EMC[tier.ordinal()];
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }
}