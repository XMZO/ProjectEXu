package me.shepherd23333.projectex.item;

import me.shepherd23333.projecte.gameObjs.items.KleinStar;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

import java.math.BigInteger;

/**
 * @author LatvianModder
 */
public class ItemColossalStar extends ItemMagnumStar {
    public ItemColossalStar(KleinStar.EnumKleinTier t) {
        super(t);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public BigInteger getMaximumEmc(ItemStack stack) {
        return STAR_EMC[tier.ordinal() + 6];
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }
}