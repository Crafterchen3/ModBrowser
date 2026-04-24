package com.deckerpw.modbrowser.gui;

import com.deckerpw.modbrowser.AutoUpdate;
import com.deckerpw.modbrowser.ModBrowser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class SetupScreen extends Screen {

    private Button[] buttons;

    private Screen lastScreen;

    private int selected = 0;

    public SetupScreen(Component p_96550_, Screen lastScreen) {
        super(p_96550_);
        this.lastScreen = lastScreen;
    }
    @Override
    protected void init() {
        super.init();
        buttons = new Button[]{
            new Button(width / 2 - 100, height / 2 - 10, 200, 20, new TranslatableComponent("setup.no_auto"), (p_96791_) -> {
                setSelected(0);
            }),
            new Button(width / 2 - 100, height / 2 + 15, 200, 20, new TranslatableComponent("setup.auto_inform"), (p_96791_) -> {
                setSelected(1);
            }),
            new Button(width / 2 - 100, height / 2 + 15 + 25, 200, 20, new TranslatableComponent("setup.auto_install"), (p_96791_) -> {
                setSelected(2);
            }),
        };
        addRenderableWidget(buttons[0]);
        addRenderableWidget(buttons[1]);
        addRenderableWidget(buttons[2]);
        setSelected(ModBrowser.ModBrowserConfigs.AUTO_UPDATE_BEHAVIOR.get().ordinal());
        addRenderableWidget(new Button(this.width / 2 - 100, this.height - 28, 200, 20, CommonComponents.GUI_DONE, (p_96257_) -> {
            onClose();
        }));
    }

    @Override
    public void render(PoseStack p_96562_, int p_96563_, int p_96564_, float p_96565_) {
        renderBackground(p_96562_);
        super.render(p_96562_, p_96563_, p_96564_, p_96565_);
        drawCenteredString(p_96562_, this.font, this.title, this.width / 2, 8, 16777215);
        drawCenteredString(p_96562_, this.font, new TranslatableComponent("setup.instruction").withStyle(ChatFormatting.BOLD), this.width / 2, height / 2 - 15 - 25 - 8, 16777215);
    }

    private void setSelected(int i) {
        buttons[selected].active = true;
        buttons[i].active = false;
        selected = i;
    }

    @Override
    public void onClose() {
        ModBrowser.ModBrowserConfigs.FIRST_START.set(false);
        ModBrowser.ModBrowserConfigs.AUTO_UPDATE_BEHAVIOR.set(AutoUpdate.AutoUpdateBehavior.values()[selected]);
        minecraft.setScreen(lastScreen);
    }
}
