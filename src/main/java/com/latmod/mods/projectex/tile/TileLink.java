package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.ProjectEXConfig;
import com.latmod.mods.projectex.ProjectEXUtils;
import com.latmod.mods.projectex.integration.PersonalEMC;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.PlayerAttemptCondenserSetEvent;
import moze_intel.projecte.api.tile.IEmcAcceptor;
import moze_intel.projecte.config.ProjectEConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class TileLink extends TileEntity implements IItemHandlerModifiable, ITickable, IEmcAcceptor {
    public final ItemStack[] inputSlots, outputSlots;
    public UUID owner = new UUID(0L, 0L);
    public String name = "";
    public BigInteger storedEMC = BigInteger.ZERO;
    private boolean isDirty = false;

    public TileLink(int in, int out) {
        inputSlots = new ItemStack[in];
        outputSlots = new ItemStack[out];
        Arrays.fill(inputSlots, ItemStack.EMPTY);
        Arrays.fill(outputSlots, ItemStack.EMPTY);
    }

    public boolean learnItems() {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        owner = nbt.getUniqueId("owner");
        name = nbt.getString("name");
        String emcs = nbt.getString("emc");
        storedEMC = emcs.isEmpty() ? BigInteger.ZERO : new BigInteger(emcs);

        Arrays.fill(inputSlots, ItemStack.EMPTY);
        Arrays.fill(outputSlots, ItemStack.EMPTY);

        NBTTagList inputList = nbt.getTagList("input", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < inputList.tagCount(); i++) {
            NBTTagCompound nbt1 = inputList.getCompoundTagAt(i);
            inputSlots[nbt1.getByte("Slot")] = new ItemStack(nbt1);
        }

        NBTTagList outputList = nbt.getTagList("output", Constants.NBT.TAG_COMPOUND);

        if (outputList.isEmpty())
            outputSlots[0] = ProjectEXUtils.fixOutput(new ItemStack(nbt.getCompoundTag("output")));
        else {
            for (int i = 0; i < outputList.tagCount(); i++) {
                NBTTagCompound nbt1 = outputList.getCompoundTagAt(i);
                outputSlots[nbt1.getByte("Slot")] = ProjectEXUtils.fixOutput(new ItemStack(nbt1));
            }
        }

        super.readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setUniqueId("owner", owner);
        nbt.setString("name", name);

        if (storedEMC.compareTo(BigInteger.ZERO) > 0)
            nbt.setString("emc", storedEMC.toString());

        NBTTagList outputList = new NBTTagList();

        for (int i = 0; i < outputSlots.length; i++) {
            outputSlots[i].setCount(1);

            if (!outputSlots[i].isEmpty()) {
                NBTTagCompound nbt1 = outputSlots[i].serializeNBT();
                nbt1.setByte("Slot", (byte) i);
                outputList.appendTag(nbt1);
            }
        }

        nbt.setTag("output", outputList);

        NBTTagList inputList = new NBTTagList();

        for (int i = 0; i < inputSlots.length; i++) {
            if (!inputSlots[i].isEmpty()) {
                NBTTagCompound nbt1 = inputSlots[i].serializeNBT();
                nbt1.setByte("Slot", (byte) i);
                inputList.appendTag(nbt1);
            }
        }

        nbt.setTag("input", inputList);
        return super.writeToNBT(nbt);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this : super.getCapability(capability, side);
    }

    @Override
    public int getSlots() {
        return inputSlots.length + outputSlots.length;
    }

    public boolean hasOwner() {
        return owner.getLeastSignificantBits() != 0L || owner.getMostSignificantBits() != 0L;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < inputSlots.length)
            return inputSlots[slot];

        if (ProjectEXConfig.general.emc_link_max_out <= 0)
            return ItemStack.EMPTY;

        int index = slot - inputSlots.length;

        if (world.isRemote || !hasOwner())
            return ItemStack.EMPTY;

        outputSlots[index].setCount(1);

        if (outputSlots[index].isEmpty())
            return ItemStack.EMPTY;

        BigInteger value = ProjectEAPI.getEMCProxy().getValue(outputSlots[index]);

        if (value.compareTo(BigInteger.ZERO) > 0) {
            int c = getCount(PersonalEMC.get(world, owner), value, ProjectEXConfig.general.emc_link_max_out);

            if (c <= 0)
                return ItemStack.EMPTY;

            outputSlots[index].setCount(c);
            return outputSlots[index];
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot < inputSlots.length) {
            inputSlots[slot] = stack;
            markDirty();
        }
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot >= inputSlots.length || !ProjectEAPI.getEMCProxy().hasValue(stack))
            return stack;

        int limit = stack.getMaxStackSize();

        if (!inputSlots[slot].isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, inputSlots[slot]))
                return stack;

            limit -= inputSlots[slot].getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (inputSlots[slot].isEmpty())
                inputSlots[slot] = reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack;
            else
                inputSlots[slot].grow(reachedLimit ? limit : stack.getCount());

            markDirty();
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public void markDirty() {
        isDirty = true;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot < inputSlots.length && ProjectEAPI.getEMCProxy().hasValue(stack);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < inputSlots.length || amount <= 0 || world.isRemote || !hasOwner())
            return ItemStack.EMPTY;

        int index = slot - inputSlots.length;
        outputSlots[index].setCount(1);

        if (outputSlots[index].isEmpty())
            return ItemStack.EMPTY;

        BigInteger value = ProjectEAPI.getEMCProxy().getValue(outputSlots[index]);

        if (value.compareTo(BigInteger.ZERO) <= 0L)
            return ItemStack.EMPTY;

        IKnowledgeProvider provider = PersonalEMC.get(world, owner);

        if (storedEMC.compareTo(value) < 0 && (provider == null || provider.getEMC().compareTo(value) < 0))
            return ItemStack.EMPTY;

        ItemStack stack = outputSlots[index].copy();
        stack.setCount(getCount(provider, value, Math.min(amount, outputSlots[index].getMaxStackSize())));

        if (stack.getCount() >= 1) {
            if (!simulate) {
                BigInteger v = value.multiply(BigInteger.valueOf(stack.getCount()));

                if (storedEMC.compareTo(v) >= 0) {
                    storedEMC = storedEMC.subtract(v);
                    markDirty();
                } else if (provider != null)
                    PersonalEMC.remove(provider, v);
            }

            return stack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot < inputSlots.length ? 64 : ProjectEXConfig.general.emc_link_max_out;
    }

    @Override
    public void onLoad() {
        if (world.isRemote)
            world.tickableTileEntities.remove(this);

        validate();
    }

    @Override
    public void update() {
        if (world.isRemote)
            return;

        if (hasOwner()) {
            IKnowledgeProvider provider = PersonalEMC.get(world, owner);
            boolean syncKnowledge = false;

            for (int i = 0; i < inputSlots.length; i++) {
                if (!inputSlots[i].isEmpty()) {
                    BigDecimal value = new BigDecimal(ProjectEAPI.getEMCProxy().getValue(inputSlots[i]));

                    if (value.compareTo(BigDecimal.ZERO) > 0) {
                        if (provider != null && learnItems())
                            syncKnowledge = provider.addKnowledge(ProjectEXUtils.fixOutput(inputSlots[i]));

                        storedEMC = storedEMC.add(value
                                .multiply(BigDecimal.valueOf(inputSlots[i].getCount()))
                                .multiply(BigDecimal.valueOf(ProjectEConfig.difficulty.covalenceLoss))
                                .toBigInteger()
                        );
                        inputSlots[i] = ItemStack.EMPTY;
                        markDirty();
                    }
                }
            }

            if (provider != null) {
                if (storedEMC.compareTo(BigInteger.ZERO) > 0) {
                    PersonalEMC.add(provider, storedEMC);
                    storedEMC = BigInteger.ZERO;
                    markDirty();
                }

                if (syncKnowledge) {
                    EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);

                    if (player != null)
                        provider.sync(player);
                }
            }
        }

        if (isDirty) {
            isDirty = false;
            world.markChunkDirty(pos, this);
        }
    }

    public int getCount(@Nullable IKnowledgeProvider knowledgeProvider, BigInteger value, int limit) {
        BigInteger emc = knowledgeProvider == null ? storedEMC : knowledgeProvider.getEMC();

        if (emc.compareTo(value) < 0)
            return 0;

        return emc.divide(value).min(BigInteger.valueOf(limit)).intValueExact();
    }

    @Override
    public BigInteger acceptEMC(EnumFacing facing, BigInteger v) {
        if (!world.isRemote) {
            storedEMC = storedEMC.add(v);
            markDirty();
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

    public boolean setOutputStack(EntityPlayer player, int slot, ItemStack stack, boolean addKnowledge) {
        stack = ProjectEXUtils.fixOutput(stack);
        IKnowledgeProvider knowledgeProvider = PersonalEMC.get(player);

        if (addKnowledge)
            ProjectEXUtils.addKnowledge(player, knowledgeProvider, stack);

        if (ProjectEAPI.getEMCProxy().hasValue(stack) && (addKnowledge || knowledgeProvider.hasKnowledge(stack))) {
            if (!MinecraftForge.EVENT_BUS.post(new PlayerAttemptCondenserSetEvent(player, stack))) {
                outputSlots[slot] = stack;
                markDirty();
            }

            return true;
        }

        return false;
    }
}