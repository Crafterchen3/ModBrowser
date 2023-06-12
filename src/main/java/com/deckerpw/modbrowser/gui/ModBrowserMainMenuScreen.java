package com.deckerpw.modbrowser.gui;

import com.deckerpw.modbrowser.ModBrowser;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class ModBrowserMainMenuScreen extends TitleScreen {

    public ModBrowserMainMenuScreen(boolean b) {
        super(b);
    }

    @Override
    protected void init() {
        super.init();
        int l = this.height / 4 + 48;
        this.addRenderableWidget(new ImageButton(this.width / 2 - 100, l + 24 * 2, 20, 20, 0, 32, 20, new ResourceLocation(ModBrowser.MOD_ID,"textures/gui/widgets.png"), 256, 256, (p_96791_) -> {
            this.minecraft.setScreen(new BrowseScreen(this));
        }));
        for (int i = 0; i < this.renderables.size(); i++) {
            Widget widget = this.renderables.get(i);
            if (widget instanceof Button btn && btn.getMessage().getString().equals(new TranslatableComponent("fml.menu.mods").getString())){
                this.renderables.remove(i);
                this.removeWidget(this.children().get(i));
                break;
            }
        }
        this.addRenderableWidget(new Button(this.width / 2 - 100+24, l + 24 * 2, 98-24, 20, new TranslatableComponent("fml.menu.mods"), button -> {
            this.minecraft.setScreen(new net.minecraftforge.client.gui.ModListScreen(this));
        }));
    }
}
