package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.objects.Mod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;

public class GuiCart extends GuiScreen {

    public GuiGetMods parent;
    private GuiSlotModList guiSlotModList;
    private int selected;

    public GuiCart(GuiGetMods parent){
        this.parent = parent;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        guiSlotModList.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);

    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButton(220,this.width-110,20,90,20,"Install all"));
        this.buttonList.add(new GuiButton(221,this.width-110,50,90,20,"Install this"));
        this.buttonList.add(new GuiButton(222,this.width-110,80,90,20,"Remove"));
        this.buttonList.add(new GuiButton(223,this.width-110,110,90,20,"Clear"));
        this.buttonList.add(new GuiButton(224,this.width-110,this.height-40,90,20,"Cancel"));

        guiSlotModList = new GuiSlotModList(mc,this.width-150,this.height-40,20,this.height-20,20,30,parent.cartlist,this);
    }

    public void elementClicked(int index){
        this.selected = index;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if(button.enabled){
            if(button.id == 222){
                parent.cartlist.remove(selected);
                guiSlotModList.setMods(parent.cartlist);
            }
            if(button.id == 223){
                parent.cartlist.clear();
                guiSlotModList.setMods(parent.cartlist);
            }
            if(button.id == 224){
                mc.displayGuiScreen(parent);
            }
        }

        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                if(button.enabled) {
                    if (button.id == 220) {
                        for (Mod item : parent.cartlist) {
                            try {
                                parent.curseforge.dowloadMod(item.getId());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        parent.cartlist.clear();
                        guiSlotModList.setMods(parent.cartlist);
                    }
                    if (button.id == 221) {
                        try {
                            parent.curseforge.dowloadMod(parent.cartlist.get(selected).getId());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        parent.cartlist.remove(selected);
                        guiSlotModList.setMods(parent.cartlist);
                    }
                }
            }
        };

        thread.start();

    }




    public boolean isSelected(int index) {
        return selected == index;
    }
}
