package com.deckerpw.modbrowser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.versioning.InvalidVersionSpecificationException;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Mod(modid = ModBrowser.MODID, name = ModBrowser.NAME, version = ModBrowser.VERSION, acceptedMinecraftVersions = "[1.12.2,1.16.5]")

public class ModBrowser
{
    public static final String MODID = "modbrowser";
    public static final String NAME = "ModBrowser";
    public static final String VERSION = "1.2";
    public static VersionRange vrs;

    static {
        try {
            vrs = VersionRange.createFromVersionSpec("[1.12,)");
        } catch (InvalidVersionSpecificationException e) {
            e.printStackTrace();
        }
    }




    public static String MODPATH;
    public static String MCPATH;
    private static Logger logger;
    private static Minecraft mc = Minecraft.getMinecraft();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        MCPATH = mc.mcDataDir.getPath();
        MODPATH = Paths.get(MCPATH,"mods").toString();
        System.out.println(MODPATH);
        System.out.println(mc.mcDataDir.getPath());
        MinecraftForge.EVENT_BUS.register(this);
    }




    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
        ConfigManager.sync(MODID, Config.Type.INSTANCE);

    }

    //to start my gui instead of the default.
    @Mod.EventBusSubscriber(Side.CLIENT)
    public static class MyStaticClientOnlyEventHandler {
        @SubscribeEvent
        public static void openGui(GuiOpenEvent event) {
            System.out.println("Found GUI LOL: "+event.getGui());
            if (event.getGui() instanceof GuiMainMenu){
                event.setGui(new com.deckerpw.modbrowser.GuiMainMenu());
            }
        }

    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MODID))
        {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
        }
    }

    @Config(modid = MODID,type = Config.Type.INSTANCE)
    public static class ModBrowserConfig {

        @Config.Name("Automatically search and add Dependencies")
        public static boolean SearchDependencies = true;
        @Config.Name("Sorting Field")
        public static SortType sortType = ModBrowserConfig.SortType.FEATURED;
        @Config.Name("Sorting Order")
        public static SortOrder sortOrder = SortOrder.DESCENDING;


        public enum SortType {
            FEATURED(1),
            POPULARITY(2),
            LAST_UPDATED(3),
            NAME(4),
            AUTHOR(5),
            TOTAL_DOWNLOADS(6),
            CATEGORY(7),
            GAME_VERSION(8);


            public int value;
            SortType(int value){
                this.value = value;
            }
        }

        public enum SortOrder {
            ASCENDING("asc"),
            DESCENDING("desc");


            public String value;
            SortOrder(String value){
                this.value = value;
            }
        }

    }


}
