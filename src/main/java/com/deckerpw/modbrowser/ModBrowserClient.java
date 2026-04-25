package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.gui.components.TexturedIconButton;
import com.deckerpw.modbrowser.gui.screens.BrowseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ModBrowser.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ModBrowser.MODID, value = Dist.CLIENT)
public class ModBrowserClient {
    public ModBrowserClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof TitleScreen))
            return;

        // NeoForge/FML's mods button translation key (same one Create uses)
        String modsText = I18n.get("fml.menu.mods");

        AbstractWidget modsWidget = event.getListenersList().stream()
                .filter(w -> w instanceof AbstractWidget)
                .map(w -> (AbstractWidget) w)
                .filter(w -> w.getMessage().getString().equals(modsText))
                .findFirst()
                .orElse(null);

        if (modsWidget == null)
            return; // Mods button not present (or key changed)

        int myW = 20, myH = 20;
        int padding = 2;

        ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(
                ModBrowser.MODID, "textures/gui/buttons/browse_button.png"
        );

        int x = modsWidget.getX() - myW - padding;
        int y = modsWidget.getY();

        event.addListener(new TexturedIconButton(x, y, myW, myH, ICON, b -> {
            Minecraft.getInstance().setScreen(new BrowseScreen(screen));
        }));
    }
}
