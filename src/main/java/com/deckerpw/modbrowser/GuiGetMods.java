package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.objects.File;
import com.deckerpw.modbrowser.objects.Mod;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.ArrayList;

public class GuiGetMods extends GuiScreen {

    private GuiTextField search;
    private int selected;
    private String title = "Get Mods!";
    private String searchFilter;
    private GuiSlotModList guiSlotModList;
    private ArrayList<Mod> modlist;
    private int page;
    private GuiInfo info;
    private GuiScreen lastScreen;
    public ArrayList<File> cartlist = new ArrayList<File>();
    public FontRenderer fontRenderer;
    public Curseforge curseforge;

    public GuiGetMods(GuiScreen lastScreen){
        page = 0;
        searchFilter = "";
        cartlist.clear();
        this.lastScreen = lastScreen;
    }

    public boolean isSelected(int index) {
        return selected == index;
    }

    public void elementClicked(int index) {
        selected = index;
        System.out.println(selected);
        info.setMod(modlist.get(selected));

    }

    public Mod getSelectedMod(){
        return modlist.get(selected);
    }

    @Override
    public void initGui() {
        super.initGui();
        //preinit:
        this.fontRenderer = mc.fontRenderer;
        curseforge = new Curseforge();
        System.out.println("PAGE= "+ page);
        try {
            modlist = curseforge.getMods("1.12.2","",page);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //init Buttons:
        this.buttonList.add(new GuiButton(200,this.width / 2 + 20,this.height - 40,this.width / 2 -40,20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(201,20,this.height-40,20,20,"<"));
        this.buttonList.add(new GuiButton(202,50,this.height-40,20,20,">"));
        this.buttonList.add(new GuiButton(203,80,this.height-40,60,20,"Search"));
        this.buttonList.add(new GuiButton(205,this.width - 85,this.height-75,60,20,"Add"));
        this.buttonList.add(new TextureButton(206,this.width-40,5,20,20,"","modbrowser:textures/gui/carticon.png"));
        //this.buttonList.add(new TextureButton(207,20,5,20,20,"","modbrowser:textures/gui/settingsicon.png"));



        //init List & Searchbar:
        this.guiSlotModList = new GuiSlotModList(mc,this.width / 2 - 40,this.height - 85,35,this.height - 50,20,33,modlist, this);
        this.info = new GuiInfo(mc,this.width/2-40,this.height - 85,35,this.height - 50,this.width/2+20,60,this);
        this.search = new GuiTextField(0, fontRenderer, 150, this.height - 40, this.width / 2-20-150, 20);
        this.search.setFocused(true);
        this.search.setCanLoseFocus(true);



    }



    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        search.textboxKeyTyped(typedChar,keyCode);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        search.updateCursorCounter();
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        try {
            info.setMod(modlist.get(selected));
        } catch (Exception e) {
            e.printStackTrace();
            Mod errormod = new Mod();
            errormod.description = "Unfortunately the description of the mod can not be shown because of non-unicode Letters.  ):";
            info.setMod(errormod);
        }
        guiSlotModList.drawScreen(mouseX,mouseY,partialTicks);
        info.drawScreen(mouseX,mouseY,partialTicks);
        this.drawCenteredString(this.fontRenderer, this.title, (this.width / 4) * 3, 15, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
        search.drawTextBox();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);


        if(button.enabled){
            if(button.id == 200){
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(lastScreen);
                lastScreen.initGui();
            }
        }

        Thread action = new Thread(){
            @Override
            public void run() {
                super.run();
                if(button.enabled){
                    if(button.id == 201){
                        if (page != 0){
                            selected = 0;
                            page = page-20;
                            System.out.println(page);
                            try {
                                sleep(10);
                                modlist = curseforge.getMods("1.12.2",searchFilter,page);
                                guiSlotModList.setMods(modlist);
                            } catch (InterruptedException | IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }
                    if(button.id == 202){
                        selected = 0;
                        page = page + 20;
                        System.out.println(page);
                        try {
                            sleep(10);
                            modlist = curseforge.getMods("1.12.2",searchFilter,page);
                            guiSlotModList.setMods(modlist);
                        } catch (InterruptedException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(button.id == 203){
                        selected = 0;
                        page = 0;
                        searchFilter = search.getText();
                        try {
                            sleep(10);
                            modlist = curseforge.getMods("1.12.2",searchFilter,page);
                            guiSlotModList.setMods(modlist);
                        } catch (InterruptedException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(button.id == 205){
                        boolean exists = false;
                        for (File file: cartlist) {
                            if (file.mod.id == modlist.get(selected).id){
                                exists = true;
                            }
                        }
                        if (!exists){

                            try {
                                cartlist.add(curseforge.getModFile(modlist.get(selected).id));
                                cartlist.addAll(curseforge.getDependencies(modlist.get(selected).id));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        };

        action.start();
        if(button.enabled){
            if(button.id == 206){

                mc.displayGuiScreen(new GuiCart(this));
            }
            //TODO: Add a Settings Gui and Settings Class
            //if(button.id == 207){
            //    mc.displayGuiScreen(new GuiSettings(this));
            //}
        }

    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }


}
