package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.gui.ModBrowserMainMenuScreen;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModBrowser.MOD_ID)
public class ModBrowser {

    public final static String MOD_ID = "modbrowser";
    public final static String MC_VERSION = "1.18.2";
    public final static String GHOST_ID = "-54";

    public ModBrowser() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,ModBrowserConfigs.SPEC,"modbrowser-client.toml");
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
        System.out.println("USER: "+ Minecraft.getInstance().getUser().getSessionId());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Some example code to dispatch IMC to another mod
    }

    private void processIMC(final InterModProcessEvent event) {
        // Some example code to receive and process InterModComms from other mods
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
    }

    public enum ModType {
        MODS("/mods", 6),
        RESOURCE_PACKS("/resourcepacks", 12),
        FILES("",0);
        public String prefix;
        int classId;

        ModType(String prefix, int classId) {
            this.prefix = prefix;
            this.classId = classId;
        }
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // Register a new block here
        }
    }

    @SubscribeEvent
    public void onMainMenu(ScreenOpenEvent e) {
        if (e.getScreen() != null && e.getScreen().getClass() == TitleScreen.class) {
            e.setScreen(new ModBrowserMainMenuScreen(true));
        } else {
            e.setResult(Event.Result.ALLOW);
        }
    }

    public class ModBrowserConfigs {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.ConfigValue<String> REFRESH_TOKEN;

        static {
            BUILDER.push("Configs for Mod Browser");
            REFRESH_TOKEN = BUILDER.define("Refresh Token","");
            BUILDER.pop();
            SPEC = BUILDER.build();
        }

    }


}
