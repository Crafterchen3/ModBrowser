package com.deckerpw.modbrowser;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

import java.io.IOException;

public class GuiSettings extends GuiScreen {

    private final GuiGetMods parent;

    public GuiSettings(GuiGetMods parent) { this.parent = parent;}

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "Settings", this.width / 2, 15, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        super.initGui();

        // init Buttons
        this.buttonList.add(new GuiButton(230, this.width / 2 - 100, this.height - 27, I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                if(button.enabled) {
                    if (button.id == 230) {
                        mc.displayGuiScreen(parent);
                        parent.initGui();
                    }
                }
            }
        };
        thread.start();
    }
}

