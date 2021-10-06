package com.deckerpw.modbrowser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.GuiOpenEvent;
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
    public static File SOURCE;
    public static String SOURCEPATH;
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
    public static String NODEPATH;
    private static Logger logger;
    private static Minecraft mc = Minecraft.getMinecraft();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        SOURCE = event.getSourceFile();
        SOURCEPATH = SOURCE.getPath();
        MCPATH = SOURCEPATH.replace("mods\\"+SOURCE.getName(),"");
        MODPATH = SOURCEPATH.replace(SOURCE.getName(),"");
        NODEPATH = MCPATH + "ModBrowser\\";
        System.out.println(MODPATH);
        System.out.println(mc.mcDataDir.getPath());
    }




    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

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
}
