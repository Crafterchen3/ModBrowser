package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.objects.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.ArrayList;
import java.util.List;

public class GuiInfo extends GuiScrollingList {

    private GuiGetMods parent;
    private Mod mod;
    private List<String> stringList;
    private List<ITextComponent> lines = null;


    public GuiInfo(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, GuiGetMods rparent) {
        super(client, width, height, top, bottom, left, entryHeight);
        this.parent = rparent;
        this.mod = parent.getSelectedMod();
        stringList = new ArrayList<String>();
        stringList.add(this.mod.description);
        lines = resizeContent(stringList);
        this.setHeaderInfo(true,getHeaderHeight());
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mod = parent.getSelectedMod();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int getHeaderHeight(){
        return parent.height;
    }

    private List<ITextComponent> resizeContent(List<String> lines)
    {
        List<ITextComponent> ret = new ArrayList<ITextComponent>();
        for (String line : lines)
        {
            if (line == null)
            {
                ret.add(null);
                continue;
            }

            ITextComponent chat = ForgeHooks.newChatWithLinks(line, false);
            int maxTextLength = this.listWidth - 8;
            if (maxTextLength >= 0)
            {
                ret.addAll(GuiUtilRenderComponents.splitText(chat, maxTextLength, parent.fontRenderer, false, true));
            }
        }
        return ret;
    }


    @Override
    protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {
        int top = relativeY;
        for (ITextComponent line : this.lines)
        {
            if (line != null)
            {
                GlStateManager.enableBlend();
                parent.fontRenderer.drawStringWithShadow(line.getFormattedText(), this.left + 4, top, 0xFFFFFF);
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
            }
            top += 10;
        }
    }

    @Override
    protected void clickHeader(int x, int y) {
        super.clickHeader(x, y);
    }

    public void setMod(Mod mod) {
        this.mod = mod;
        String str = mod.name +"\nmade by: "+mod.authors+"\n\n"+mod.description;
        this.stringList.clear();
        this.stringList.add(str);
        this.lines = resizeContent(stringList);
    }

    @Override
    protected int getSize() {
        return 0;
    }
    @Override
    protected void elementClicked(int index, boolean doubleClick) {

    }
    @Override
    protected boolean isSelected(int index) {
        return false;
    }
    @Override
    protected void drawBackground() {

    }
    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {

    }
}
