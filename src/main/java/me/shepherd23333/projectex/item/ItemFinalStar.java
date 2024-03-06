package me.shepherd23333.projectex.item;

import me.shepherd23333.projecte.api.ProjectEAPI;
import me.shepherd23333.projecte.api.item.IItemEmc;
import me.shepherd23333.projecte.api.item.IPedestalItem;
import me.shepherd23333.projecte.utils.NBTWhitelist;
import me.shepherd23333.projectex.ProjectEXConfig;
import me.shepherd23333.projectex.ProjectEXUtils;
import me.shepherd23333.projectex.tile.TileRelay;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemFinalStar extends Item implements IItemEmc, IPedestalItem {
    public ItemFinalStar() {
        setMaxStackSize(1);
    }

    @Override
    public BigInteger addEmc(ItemStack stack, BigInteger toAdd) {
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger extractEmc(ItemStack stack, BigInteger toRemove) {
        return toRemove;
    }

    @Override
    public BigInteger getStoredEmc(ItemStack stack) {
        return BigInteger.valueOf(Long.MAX_VALUE);
    }

    @Override
    public BigInteger getMaximumEmc(ItemStack stack) {
        return ProjectEXUtils.MAX_EMC;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public void updateInPedestal(World world, BlockPos pos) {
        if (ProjectEXConfig.general.final_star_update_interval <= 0) {
            return;
        }

        if (!world.isRemote && world.getTotalWorldTime() % (long) ProjectEXConfig.general.final_star_update_interval == TileRelay.mod(pos.hashCode(), ProjectEXConfig.general.final_star_update_interval)) {
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, Block.FULL_BLOCK_AABB.offset(pos).expand(0D, 1D, 0D));

            if (!items.isEmpty()) {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    if (facing != EnumFacing.UP) {
                        TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
                        IItemHandler handler = tileEntity == null ? null : tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());

                        if (handler != null) {
                            ItemStack stack = items.get(world.rand.nextInt(items.size())).getItem().copy();

                            if (ProjectEXConfig.general.final_star_copy_any_item || ProjectEAPI.getEMCProxy().hasValue(stack)) {
                                stack.setCount(stack.getMaxStackSize());

                                if (!stack.getHasSubtypes() && stack.isItemStackDamageable()) {
                                    stack.setItemDamage(0);
                                }

                                if (!ProjectEXConfig.general.final_star_copy_nbt && stack.hasTagCompound() && !NBTWhitelist.shouldDupeWithNBT(stack)) {
                                    stack.setTagCompound(new NBTTagCompound());
                                }

                                ItemHandlerHelper.insertItem(handler, stack, false);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<String> getPedestalDescription() {
        return Collections.singletonList(I18n.format("item.projectex.final_star.tooltip"));
    }
}