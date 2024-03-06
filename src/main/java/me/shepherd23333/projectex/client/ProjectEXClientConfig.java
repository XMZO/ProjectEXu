package me.shepherd23333.projectex.client;

import me.shepherd23333.projectex.ProjectEX;
import net.minecraftforge.common.config.Config;

/**
 * @author LatvianModder
 */
@Config(modid = ProjectEX.MOD_ID, category = "", name = "../local/client/" + ProjectEX.MOD_ID)
@Config.LangKey(ProjectEX.MOD_ID + "_client")
public class ProjectEXClientConfig {
    @Config.LangKey("stat.generalButton")
    public static final General general = new General();

    public static class General {
        public EnumScreenPosition emc_screen_position = EnumScreenPosition.TOP_LEFT;

        public EnumSearchType search_type = EnumSearchType.NORMAL;
    }
}