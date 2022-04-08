/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.objects.File;
import com.deckerpw.modbrowser.objects.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.ArrayList;

/**
 * @author cpw
 *
 */
public class GuiSlotFileList extends GuiScrollingList
{

    private ArrayList<File> files;
    private GuiGetMods parent;
    private GuiCart parent2;
    private boolean isCart;


    public GuiSlotFileList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, ArrayList<File> files, GuiCart parent) {
        super(client, width, height, top, bottom, left, entryHeight);
        this.files = files;
        this.parent2 = parent;
        this.isCart = true;
    }



    @Override
    protected int getSize()
    {
        return files.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        if(isCart){
            parent2.elementClicked(index);
        }else{
            parent.elementClicked(index);
        }
    }

    @Override
    protected boolean isSelected(int index) {
        if(isCart){
            return parent2.isSelected(index);
        }else{
            return parent.isSelected(index);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawBackground() {

    }

    protected void setFiles(ArrayList<File> files){
        this.files = files;
    }

    @Override
    protected void drawSlot(int idx, int right, int top, int height, Tessellator tess){
        FontRenderer font;
        if (isCart){
            font = this.parent2.parent.getFontRenderer();
        }else {
            font = this.parent.getFontRenderer();
        }
        font.drawString(font.trimStringToWidth(files.get(idx).mod.name,    listWidth - 10), this.left + 3 , top +  2, 0xFFFFFF);
        font.drawString(font.trimStringToWidth(files.get(idx).mod.authors, listWidth - (5 + height)), this.left + 3 , top + 12, 0xCCCCCC);
    }
}
