package me.shepherd23333.projectex;

import me.shepherd23333.projecte.PECore;
import me.shepherd23333.projectex.gui.ProjectEXGuiHandler;
import me.shepherd23333.projectex.item.ProjectEXItems;
import me.shepherd23333.projectex.net.ProjectEXNetHandler;
import me.shepherd23333.projectex.tile.AlchemyTableRecipes;
import me.shepherd23333.projectex.tile.TilePowerFlower;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = ProjectEX.MOD_ID,
        name = ProjectEX.MOD_NAME,
        version = Tags.VERSION,
        dependencies = "required-after:" + PECore.MODID
)
public class ProjectEX {
    public static final String MOD_ID = "projectex";
    public static final String MOD_NAME = "Project EX";

    public static final CreativeTabs TAB = new CreativeTabs(MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ProjectEXItems.PERSONAL_LINK);
        }
    };

    @Mod.Instance(MOD_ID)
    public static ProjectEX INSTANCE;

    @SidedProxy(serverSide = "me.shepherd23333.projectex.ProjectEXCommon", clientSide = "me.shepherd23333.projectex.client.ProjectEXClient")
    public static ProjectEXCommon PROXY;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        ProjectEXNetHandler.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ProjectEXGuiHandler());
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
            ProjectEXKeyBindings.init();

        if (ProjectEXConfig.general.blacklist_power_flower_from_watch)
            FMLInterModComms.sendMessage(PECore.MODID, "timewatchblacklist", TilePowerFlower.class.getName());

        AlchemyTableRecipes.INSTANCE.addDefaultRecipes();
    }
}