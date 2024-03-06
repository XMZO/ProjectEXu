package me.shepherd23333.projectex.tile;

import me.shepherd23333.projecte.api.tile.IEmcAcceptor;
import me.shepherd23333.projecte.api.tile.IEmcProvider;
import me.shepherd23333.projecte.gameObjs.tiles.RelayMK1Tile;
import me.shepherd23333.projectex.ProjectEXUtils;
import me.shepherd23333.projectex.block.EnumTier;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import java.math.BigInteger;

/**
 * @author LatvianModder
 */
public class TileRelay extends TileEntity implements ITickable, IEmcAcceptor, IEmcProvider {
    public static final IEmcAcceptor[] TEMP = new IEmcAcceptor[6];
    public BigInteger stored = BigInteger.ZERO;
    private boolean isDirty = false;

    public static int mod(int i, int n) {
        i = i % n;
        return i < 0 ? i + n : i;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("stored_emc", stored.toString());
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        stored = new BigInteger(nbt.getString("stored_emc"));
        super.readFromNBT(nbt);
    }

    @Override
    public void onLoad() {
        if (world.isRemote) {
            world.tickableTileEntities.remove(this);
        }

        validate();
    }

    @Override
    public void update() {
        if (world.isRemote || stored.compareTo(BigInteger.ZERO) <= 0 || world.getTotalWorldTime() % 20L != mod(hashCode(), 20)) {
            return;
        }

        int tempSize = 0;

        for (int i = 0; i < 6; i++) {
            TEMP[i] = null;
            TileEntity tileEntity = world.getTileEntity(pos.offset(EnumFacing.VALUES[i]));

            if (tileEntity instanceof IEmcAcceptor && !(tileEntity instanceof TileRelay) && !(tileEntity instanceof RelayMK1Tile)) {
                TEMP[i] = (IEmcAcceptor) tileEntity;
                tempSize++;
            }
        }

        if (tempSize > 0) {
            BigInteger s = stored.divide(BigInteger.valueOf(tempSize))
                    .min(EnumTier.byMeta(getBlockMetadata()).properties.getRt().toBigInteger());

            for (int i = 0; i < 6; i++) {
                if (TEMP[i] != null) {
                    BigInteger a = TEMP[i].acceptEMC(EnumFacing.VALUES[i].getOpposite(), s);

                    if (a.compareTo(BigInteger.ZERO) > 0L) {
                        stored = stored.subtract(a);
                        markDirty();
                    }
                }
            }
        }

        if (isDirty) {
            isDirty = false;
            world.markChunkDirty(pos, this);
        }
    }

    @Override
    public void markDirty() {
        isDirty = true;
    }

    @Override
    public BigInteger acceptEMC(EnumFacing facing, BigInteger v) {
        BigInteger v1 = getMaximumEmc().subtract(stored).min(v);

        if (v1.compareTo(BigInteger.ZERO) > 0) {
            stored = stored.add(v1);
            markDirty();
        }

        return v1;
    }

    @Override
    public BigInteger provideEMC(EnumFacing facing, BigInteger v) {
        BigInteger v1 = stored.min(v);

        if (v1.compareTo(BigInteger.ZERO) > 0) {
            stored = stored.subtract(v1);
            markDirty();
        }

        return v1;
    }

    @Override
    public BigInteger getStoredEmc() {
        return stored;
    }

    @Override
    public BigInteger getMaximumEmc() {
        return ProjectEXUtils.MAX_EMC;
    }

    public void addRelayBonus(EnumFacing facing) {
        acceptEMC(facing, EnumTier.byMeta(getBlockMetadata()).properties.getRb().toBigInteger());
    }
}