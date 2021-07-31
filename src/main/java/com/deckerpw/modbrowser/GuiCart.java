package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.objects.File;
import com.deckerpw.modbrowser.objects.Mod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;

public class GuiCart extends GuiScreen {

    public GuiGetMods parent;
    private GuiSlotModList guiSlotModList;
    private int selected;
    private GuiLog log;

    public GuiCart(GuiGetMods parent){
        this.parent = parent;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        guiSlotModList.drawScreen(mouseX,mouseY,partialTicks);
        log.drawScreen(mouseX,mouseY,partialTicks);
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

        guiSlotModList = new GuiSlotModList(mc,(this.width-150)/2,this.height-40,20,this.height-20,20,30,parent.cartlist,this);
        this.log = new GuiLog(mc,(this.width-150)/2,this.height-40,20,this.height - 20,((this.width/2)-(((this.width-150)/2)/2))+20,60,this);
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
                        log.addText("\nStarting download:\n");
                        for (Mod item : parent.cartlist) {
                            try {
                                ArrayList<File> files =  parent.curseforge.getModFiles(item.id);
                                parent.curseforge.downloadFile(files.get(0));
                                log.addText("downloaded "+item.name);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        log.addText("\nRestart Minecraft to aquire chnages.");
                        parent.cartlist.clear();
                        guiSlotModList.setMods(parent.cartlist);
                    }
                    if (button.id == 221) {
                        try {
                            log.addText("\nStarting download\n");
                            ArrayList<File> files =  parent.curseforge.getModFiles(parent.cartlist.get(selected).id);
                            parent.curseforge.downloadFile(files.get(0));
                            log.addText("downloaded "+parent.cartlist.get(selected).name);
                            log.addText("\nRestart Minecraft to aquire chnages.");
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
