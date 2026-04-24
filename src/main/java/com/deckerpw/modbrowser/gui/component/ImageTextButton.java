package com.deckerpw.modbrowser.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ImageTextButton extends ImageButton {
    public ImageTextButton(int p_169011_, int p_169012_, int p_169013_, int p_169014_, int p_169015_, int p_169016_, ResourceLocation p_169017_, OnPress p_169018_) {
        super(p_169011_, p_169012_, p_169013_, p_169014_, p_169015_, p_169016_, p_169017_, p_169018_);
    }

    public ImageTextButton(int p_94269_, int p_94270_, int p_94271_, int p_94272_, int p_94273_, int p_94274_, int p_94275_, ResourceLocation p_94276_, OnPress p_94277_) {
        super(p_94269_, p_94270_, p_94271_, p_94272_, p_94273_, p_94274_, p_94275_, p_94276_, p_94277_);
    }

    public ImageTextButton(int p_94230_, int p_94231_, int p_94232_, int p_94233_, int p_94234_, int p_94235_, int p_94236_, ResourceLocation p_94237_, int p_94238_, int p_94239_, OnPress p_94240_) {
        super(p_94230_, p_94231_, p_94232_, p_94233_, p_94234_, p_94235_, p_94236_, p_94237_, p_94238_, p_94239_, p_94240_);
    }

    public ImageTextButton(int p_94256_, int p_94257_, int p_94258_, int p_94259_, int p_94260_, int p_94261_, int p_94262_, ResourceLocation p_94263_, int p_94264_, int p_94265_, OnPress p_94266_, Component p_94267_) {
        super(p_94256_, p_94257_, p_94258_, p_94259_, p_94260_, p_94261_, p_94262_, p_94263_, p_94264_, p_94265_, p_94266_, p_94267_);
    }

    public ImageTextButton(int p_94242_, int p_94243_, int p_94244_, int p_94245_, int p_94246_, int p_94247_, int p_94248_, ResourceLocation p_94249_, int p_94250_, int p_94251_, OnPress p_94252_, OnTooltip p_94253_, Component p_94254_) {
        super(p_94242_, p_94243_, p_94244_, p_94245_, p_94246_, p_94247_, p_94248_, p_94249_, p_94250_, p_94251_, p_94252_, p_94253_, p_94254_);
    }

    @Override
    public void renderButton(PoseStack p_94282_, int p_94283_, int p_94284_, float p_94285_) {
        super.renderButton(p_94282_, p_94283_, p_94284_, p_94285_);

        int j = getFGColor();
        drawString(p_94282_, Minecraft.getInstance().font,getMessage(),x+22,this.y + (this.height - 8) / 2,j | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
