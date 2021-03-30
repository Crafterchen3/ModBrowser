package com.deckerpw.modbrowser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = ModBrowser.MODID, name = ModBrowser.NAME, version = ModBrowser.VERSION)
public class ModBrowser
{
    public static final String MODID = "modbrowser";
    public static final String NAME = "ModBrowser";
    public static final String VERSION = "1.0";
    public static File SOURCE;
    public static String SOURCEPATH;
    public static String MODPATH;
    public static String MCPATH;
    public static String NODEPATH;
    private static Logger logger;
    private Minecraft mc = Minecraft.getMinecraft();

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
            if (event.getGui() instanceof GuiMainMenu){
                event.setGui(new com.deckerpw.modbrowser.GuiMainMenu());
            }
        }
    }
}
