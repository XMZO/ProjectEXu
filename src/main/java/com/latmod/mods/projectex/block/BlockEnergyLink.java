package com.latmod.mods.projectex.block;

import com.latmod.mods.projectex.tile.TileEnergyLink;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class BlockEnergyLink extends Block {
    public BlockEnergyLink() {
        super(Material.ROCK, MapColor.BLACK);
        setHardness(2F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEnergyLink();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);

            if (tileEntity instanceof TileEnergyLink) {
                player.sendStatusMessage(new TextComponentString(((TileEnergyLink) tileEntity).name), true);
            }
        }

        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof TileEnergyLink) {
            ((TileEnergyLink) tileEntity).owner = placer.getUniqueID();
            ((TileEnergyLink) tileEntity).name = placer.getName();
            tileEntity.markDirty();
        }
    }
}