package com.deckerpw.modbrowser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class TextureButton extends GuiButton {

    public ResourceLocation res;
    private Minecraft mc = Minecraft.getMinecraft();

    public TextureButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String Resource){
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.res = new ResourceLocation(Resource);
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(res);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = 106;

            if (flag) {
                i += this.height;
            }

            this.drawTexturedModalRect(this.x, this.y, 0, i, this.width, this.height);
        }


    }
}
