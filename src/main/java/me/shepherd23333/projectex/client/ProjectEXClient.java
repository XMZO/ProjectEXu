package me.shepherd23333.projectex.client;

import me.shepherd23333.projectex.ProjectEXCommon;
import me.shepherd23333.projectex.integration.PersonalEMC;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.math.BigInteger;

/**
 * @author LatvianModder
 */
public class ProjectEXClient extends ProjectEXCommon {
    @Override
    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    public void updateEMC(BigInteger emc) {
        PersonalEMC.get(Minecraft.getMinecraft().player).setEmc(emc);
    }
}