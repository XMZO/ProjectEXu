package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.block.EnumTier;
import com.latmod.mods.projectex.integration.PersonalEMC;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import java.math.BigInteger;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class TilePowerFlower extends TileEntity implements ITickable {
    public UUID owner = new UUID(0L, 0L);
    public String name = "";
    public BigInteger storedEMC = BigInteger.ZERO;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        owner = nbt.getUniqueId("owner");
        name = nbt.getString("name");
        storedEMC = nbt.hasKey("emc") ? new BigInteger(nbt.getString("emc")) : BigInteger.ZERO;
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

        storedEMC = storedEMC.add(EnumTier.byMeta(getBlockMetadata()).properties.powerFlowerOutput().toBigInteger());

        EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);

        if (player != null) {
            PersonalEMC.add(PersonalEMC.get(player), storedEMC);
            storedEMC = BigInteger.ZERO;
        } else {
            world.markChunkDirty(pos, this);
        }
    }
}