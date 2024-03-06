package me.shepherd23333.projectex.block;

import me.shepherd23333.projectex.tile.TileLinkMK3;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class BlockLinkMK3 extends BlockLink {
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileLinkMK3();
    }
}