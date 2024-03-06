package me.shepherd23333.projectex.tile;

import me.shepherd23333.projecte.api.tile.IEmcAcceptor;
import me.shepherd23333.projectex.ProjectEXUtils;
import me.shepherd23333.projectex.integration.PersonalEMC;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import java.math.BigInteger;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class TileEnergyLink extends TileEntity implements ITickable, IEmcAcceptor {
    public UUID owner = new UUID(0L, 0L);
    public String name = "";
    public BigInteger storedEMC = BigInteger.ZERO;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        owner = nbt.getUniqueId("owner");
        name = nbt.getString("name");
        storedEMC = new BigInteger(nbt.getString("emc"));
        super.readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setUniqueId("owner", owner);
        nbt.setString("name", name);

        if (storedEMC.compareTo(BigInteger.ZERO) > 0) {
            nbt.setString("emc", storedEMC.toString());
        }

        return super.writeToNBT(nbt);
    }

    @Override
    public void markDirty() {
        if (world != null) {
            world.markChunkDirty(pos, this);
        }
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
        if (world.isRemote || world.getTotalWorldTime() % 20L != TileRelay.mod(hashCode(), 20)) {
            return;
        }

        if (storedEMC.compareTo(BigInteger.ZERO) > 0) {
            EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);

            if (player != null) {
                PersonalEMC.add(PersonalEMC.get(player), storedEMC);
                storedEMC = BigInteger.ZERO;
                markDirty();
            }
        }
    }

    @Override
    public BigInteger acceptEMC(EnumFacing facing, BigInteger v) {
        if (!world.isRemote) {
            storedEMC = storedEMC.add(v);
        }

        return v;
    }

    @Override
    public BigInteger getStoredEmc() {
        return storedEMC;
    }

    @Override
    public BigInteger getMaximumEmc() {
        return ProjectEXUtils.MAX_EMC;
    }
}