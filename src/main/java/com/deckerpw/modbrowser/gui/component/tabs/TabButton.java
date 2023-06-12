package com.deckerpw.modbrowser.gui.component.tabs;

import com.deckerpw.modbrowser.ModBrowser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TabButton extends Button {

    private boolean selected = false;
    private final TabManager manager;

    public TabButton(TabManager manager,int p_93721_, int p_93722_, int p_93723_, int p_93724_, Component p_93725_, OnPress p_93726_) {
        super(p_93721_, p_93722_, p_93723_, p_93724_, p_93725_, p_93726_);
        this.manager = manager;
    }

    public TabButton(TabManager manager,int p_93728_, int p_93729_, int p_93730_, int p_93731_, Component p_93732_, OnPress p_93733_, OnTooltip p_93734_) {
        super(p_93728_, p_93729_, p_93730_, p_93731_, p_93732_, p_93733_, p_93734_);
        this.manager = manager;
    }

    public void renderButton(PoseStack p_93676_, int p_93677_, int p_93678_, float p_93679_) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation(ModBrowser.MOD_ID,"textures/gui/widgets.png"));
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(p_93676_, this.x, this.y, 0, 72 + i * 24, this.width / 2, this.height);
        this.blit(p_93676_, this.x + this.width / 2, this.y, 200 - this.width / 2, 72 + i * 24, this.width / 2, this.height);
        this.renderBg(p_93676_, minecraft, p_93677_, p_93678_);
        int j = getFGColor();
        drawCenteredString(p_93676_, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2 +(!this.isSelected() ? 4:0), j | Mth.ceil(this.alpha * 255.0F) << 24);
        if (this.isHoveredOrFocused()) {
            this.renderToolTip(p_93676_, p_93677_, p_93678_);
        }

    }

    @Override
    protected int getYImage(boolean p_93668_) {
        int i = 2;
        if (this.isSelected() || !this.active){
            i=0;
        } if (p_93668_){
            i++;
        }
        return i;
    }

    private boolean isSelected() {
        return this.manager.getSelected().equals(this);
    }

    @Override
    public void onPress() {
        super.onPress();
        this.manager.setSelected(this);
    }
}
