package com.deckerpw.modbrowser.gui;

import com.deckerpw.modbrowser.Curseforge;
import com.deckerpw.modbrowser.Mod;
import com.deckerpw.modbrowser.ModBrowser;
import com.deckerpw.modbrowser.gui.component.ModSelectionList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public class BrowseScreen extends Screen {

    public static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
    public static final ResourceLocation LOADING_ICON = new ResourceLocation(ModBrowser.MOD_ID,"textures/gui/loading.png");
    private Screen lastScreen;
    private Curseforge cf;
    private Thread thread;
    private EditBox searchBox;
    private ModSelectionList.ModListEntry loadingEntry;
    private ModSelectionList modList;
    private boolean full = false;
    private int index = 0;

    public BrowseScreen(Screen lastScreen) {
        super(new TranslatableComponent("browse.name"));
        this.lastScreen = lastScreen;
    }

    public void loadMore(){
        thread.stop();
        thread = new Thread(() -> {
            if (!full){
                this.modList.children().add(loadingEntry);
                try {
                    ModSelectionList.ModListEntry entry = cf.getMods("1.18.2", searchBox.getValue(), index, 1).get(0);
                    if (entry.mod.id == ModBrowser.GHOST_ID) {
                        full = true;
                    }
                    this.modList.children().set(this.modList.children().size()-1,entry);
                    index++;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        thread.start();
    }

    private void refresh(){
        thread.stop();
        index = 0;
        modList.children().clear();
        full = false;
        this.modList.reset();
        thread = new Thread(() -> {
            if (!full){
                this.modList.children().add(loadingEntry);
                try {
                    ModSelectionList.ModListEntry entry = cf.getMods("1.18.2", searchBox.getValue(), index, 1).get(0);
                    if (entry.mod.id == ModBrowser.GHOST_ID) {
                        full = true;
                    }
                    this.modList.children().set(this.modList.children().size()-1,entry);
                    index++;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        thread.start();
    }

    @Override
    protected void init() {
        super.init();

        this.modList = new ModSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36);

        cf = new Curseforge(minecraft,modList,this);

        this.addWidget(modList);
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, new TranslatableComponent("browse.search"));
        this.searchBox.setResponder((p_101362_) -> {
            refresh();
        });

        this.addWidget(this.searchBox);

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 28, 200, 20, CommonComponents.GUI_DONE, (p_96257_) -> {
            this.minecraft.setScreen(this.lastScreen);
        }));

        this.setInitialFocus(this.searchBox);
        thread = new Thread(() -> {
            try {
                this.modList.children().addAll(cf.getMods("1.18.2", searchBox.getValue(), index, 1));
                index++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Mod loadingMod = new Mod();
        loadingMod.title = "Loading...";
        loadingMod.authors = "no-one";
        loadingMod.id = ModBrowser.GHOST_ID;
        loadingMod.description = "";
        loadingMod.category = "";
        this.loadingEntry = new ModSelectionList.ModListEntry(minecraft,modList,this,loadingMod,cf,false);
        loadMore();
    }

    public boolean keyPressed(int p_101347_, int p_101348_, int p_101349_) {
        return super.keyPressed(p_101347_, p_101348_, p_101349_) ? true : this.searchBox.keyPressed(p_101347_, p_101348_, p_101349_);
    }

    public boolean charTyped(char p_101340_, int p_101341_) {
        return this.searchBox.charTyped(p_101340_, p_101341_);
    }

    @Override
    public void render(PoseStack p_96562_, int p_96563_, int p_96564_, float p_96565_) {
        this.renderBackground(p_96562_);
        this.modList.render(p_96562_, p_96563_, p_96564_, p_96565_);
        this.searchBox.render(p_96562_, p_96563_, p_96564_, p_96565_);
        drawCenteredString(p_96562_, this.font, this.title, this.width / 2, 8, 16777215);
        super.render(p_96562_, p_96563_, p_96564_, p_96565_);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }


}
