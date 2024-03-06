package me.shepherd23333.projectex.tile;

import me.shepherd23333.projecte.api.tile.IEmcAcceptor;
import me.shepherd23333.projecte.gameObjs.tiles.RelayMK1Tile;
import me.shepherd23333.projecte.gameObjs.tiles.RelayMK2Tile;
import me.shepherd23333.projecte.gameObjs.tiles.RelayMK3Tile;
import me.shepherd23333.projectex.block.EnumTier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author LatvianModder
 */
public class TileCollector extends TileEntity implements ITickable {
    @Override
    public void onLoad() {
        if (world.isRemote) {
            world.tickableTileEntities.remove(this);
        }

        validate();
    }

    @Override
    public void update() {
        if (world.isRemote || world.getTotalWorldTime() % 20L != TileRelay.mod(hashCode(), 20)) {
            return;
        }

        int tempSize = 0;

        for (int i = 0; i < 6; i++) {
            TileRelay.TEMP[i] = null;
            TileEntity tileEntity = world.getTileEntity(pos.offset(EnumFacing.VALUES[i]));

            if (tileEntity instanceof IEmcAcceptor) {
                IEmcAcceptor emcAcceptor = (IEmcAcceptor) tileEntity;
                TileRelay.TEMP[i] = emcAcceptor;
                tempSize++;

                if (emcAcceptor instanceof TileRelay) {
                    ((TileRelay) emcAcceptor).addRelayBonus(EnumFacing.VALUES[i].getOpposite());
                } else if (emcAcceptor instanceof RelayMK3Tile) {
                    emcAcceptor.acceptEMC(EnumFacing.VALUES[i].getOpposite(), BigInteger.valueOf(10L));
                } else if (emcAcceptor instanceof RelayMK2Tile) {
                    emcAcceptor.acceptEMC(EnumFacing.VALUES[i].getOpposite(), BigInteger.valueOf(3L));
                } else if (emcAcceptor instanceof RelayMK1Tile) {
                    emcAcceptor.acceptEMC(EnumFacing.VALUES[i].getOpposite(), BigInteger.ONE);
                }
            }
        }

        if (tempSize > 0) {
            BigInteger s = EnumTier.byMeta(getBlockMetadata()).properties.getCo()
                    .divide(BigDecimal.valueOf(tempSize)).toBigInteger();

            for (int i = 0; i < 6; i++) {
                IEmcAcceptor emcAcceptor = TileRelay.TEMP[i];

                if (emcAcceptor != null) {
                    emcAcceptor.acceptEMC(EnumFacing.VALUES[i].getOpposite(), s);
                }
            }
        }
    }

    @Override
    public void markDirty() {
    }
}