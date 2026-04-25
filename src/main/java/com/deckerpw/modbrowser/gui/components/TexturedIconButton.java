package com.deckerpw.modbrowser.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class TexturedIconButton extends Button {
    private final ResourceLocation icon;

    public TexturedIconButton(int x, int y, int w, int h, ResourceLocation icon, OnPress onPress) {
        super(x, y, w, h, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.icon = icon;
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(g, mouseX, mouseY, partialTick);

        // Draw the icon centered in the button
        int iconW = 16, iconH = 16; // or 20/20 if your texture is 20x20
        int ix = getX() + (width - iconW) / 2;
        int iy = getY() + (height - iconH) / 2;

        // blit(texture, x, y, u, v, drawW, drawH, texW, texH)
        g.blit(icon, ix, iy, 0, 0, iconW, iconH, iconW, iconH);
    }
}