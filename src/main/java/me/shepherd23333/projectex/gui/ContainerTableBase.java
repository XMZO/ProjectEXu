package me.shepherd23333.projectex.gui;

import me.shepherd23333.projecte.api.ProjectEAPI;
import me.shepherd23333.projecte.api.capabilities.IKnowledgeProvider;
import me.shepherd23333.projecte.api.item.IItemEmc;
import me.shepherd23333.projecte.config.ProjectEConfig;
import me.shepherd23333.projectex.ProjectEXUtils;
import me.shepherd23333.projectex.integration.PersonalEMC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author LatvianModder
 */
public class ContainerTableBase extends Container {
    public static final int BURN = 1;
    public static final int TAKE_STACK = 2;
    public static final int TAKE_ONE = 3;
    public static final int BURN_ALT = 4;
    public static final int LEARN = 5;
    public static final int UNLEARN = 6;
    public final EntityPlayer player;
    public final IKnowledgeProvider playerData;
    public KnowledgeUpdate knowledgeUpdate;

    public ContainerTableBase(EntityPlayer p) {
        player = p;
        playerData = PersonalEMC.get(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        ItemStack stack = slot.getStack();

        if (!stack.isEmpty()) {
            if (!ProjectEAPI.getEMCProxy().hasValue(stack)) {
                return ItemStack.EMPTY;
            }

            ItemStack stack1 = ProjectEXUtils.fixOutput(stack);

            if (!isItemValid(stack1)) {
                return ItemStack.EMPTY;
            }

            int k = ProjectEXUtils.addKnowledge(player, playerData, stack1);

            if (k == 0) {
                return ItemStack.EMPTY;
            } else if (k == 2 && knowledgeUpdate != null) {
                knowledgeUpdate.updateKnowledge();
            }

            PersonalEMC.add(playerData,
                    new BigDecimal(ProjectEAPI.getEMCProxy().getValue(stack))
                            .multiply(BigDecimal.valueOf(stack.getCount()))
                            .multiply(BigDecimal.valueOf(ProjectEConfig.difficulty.covalenceLoss))
                            .toBigInteger()
            );
            slot.putStack(ItemStack.EMPTY);
            return stack1;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    public boolean isItemValid(ItemStack stack) {
        return true;
    }

    public boolean clickGuiSlot(ItemStack type, int mode) {
        ItemStack stack = player.inventory.getItemStack();

        if (mode == BURN || mode == BURN_ALT) {
            if (mode == BURN_ALT && stack.getItem() instanceof IItemEmc) {
                IItemEmc emcItem = (IItemEmc) stack.getItem();
                BigInteger stored = emcItem.getStoredEmc(stack);

                if (stored.compareTo(BigInteger.ZERO) > 0) {
                    PersonalEMC.add(playerData, emcItem.extractEmc(stack, stored));
                } else {
                    PersonalEMC.remove(playerData, emcItem.addEmc(stack, playerData.getEmc().min(emcItem.getMaximumEmc(stack))));
                }

                player.inventory.setItemStack(stack);
                return true;
            }

            if (stack.isEmpty() || !ProjectEAPI.getEMCProxy().hasValue(stack)) {
                return false;
            }

            ItemStack stack1 = ProjectEXUtils.fixOutput(stack);

            if (!isItemValid(stack1)) {
                return false;
            }

            int k = ProjectEXUtils.addKnowledge(player, playerData, stack1);

            if (k == 0) {
                return false;
            } else if (k == 2 && knowledgeUpdate != null) {
                knowledgeUpdate.updateKnowledge();
            }

            PersonalEMC.add(playerData,
                    new BigDecimal(ProjectEAPI.getEMCProxy().getValue(stack))
                            .multiply(BigDecimal.valueOf(stack.getCount()))
                            .multiply(BigDecimal.valueOf(ProjectEConfig.difficulty.covalenceLoss))
                            .toBigInteger()
            );
            player.inventory.setItemStack(ItemStack.EMPTY);
            return true;
        } else if (mode == TAKE_STACK) {
            if (type.isEmpty()) {
                return false;
            }

            ItemStack stack1 = ItemHandlerHelper.copyStackWithSize(type, 1);
            BigInteger value = ProjectEAPI.getEMCProxy().getValue(stack1);

            if (value.compareTo(BigInteger.ZERO) <= 0) {
                return false;
            }

            int amount = type.getMaxStackSize();

            BigInteger max = playerData.getEmc().divide(value);

            if (max.compareTo(BigInteger.valueOf(amount)) < 0) {
                amount = max.min(BigInteger.valueOf(amount)).intValue();
            }

            if (amount <= 0) {
                return false;
            }

            PersonalEMC.remove(playerData, value.multiply(BigInteger.valueOf(amount)));
            stack1.setCount(amount);
            player.inventory.placeItemBackInInventory(player.world, stack1);
            return true;
        } else if (mode == TAKE_ONE) {
            if (type.isEmpty()) {
                return false;
            }

            if (!stack.isEmpty() && (!ItemHandlerHelper.canItemStacksStack(type, stack) || stack.getCount() >= stack.getMaxStackSize())) {
                return false;
            }

            ItemStack stack1 = ItemHandlerHelper.copyStackWithSize(type, 1);
            BigInteger value = ProjectEAPI.getEMCProxy().getValue(stack1);

            if (value.compareTo(BigInteger.ZERO) <= 0) {
                return false;
            }

            if (playerData.getEmc().compareTo(value) < 0) {
                return false;
            }

            PersonalEMC.remove(playerData, value);

            if (!stack.isEmpty()) {
                stack.grow(1);
            } else {
                player.inventory.setItemStack(stack1);
            }

            return true;
        } else if (mode == LEARN) {
            if (stack.isEmpty() || !ProjectEAPI.getEMCProxy().hasValue(stack)) {
                return false;
            }

            ItemStack stack1 = ProjectEXUtils.fixOutput(stack);

            if (!isItemValid(stack1)) {
                return false;
            }

            int k = ProjectEXUtils.addKnowledge(player, playerData, stack1);

            if (k == 0) {
                return false;
            } else if (k == 2 && knowledgeUpdate != null) {
                knowledgeUpdate.updateKnowledge();
            }

            return true;
        } else if (mode == UNLEARN) {
            if (stack.isEmpty()) {
                return false;
            }

            if (playerData.removeKnowledge(ProjectEXUtils.fixOutput(stack))) {
                if (knowledgeUpdate != null) {
                    knowledgeUpdate.updateKnowledge();
                }

                return true;
            }

            return false;
        }

        return false;
    }

    public interface KnowledgeUpdate {
        void updateKnowledge();
    }
}