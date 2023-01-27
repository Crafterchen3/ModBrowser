package com.deckerpw.modbrowser.gui;

import com.deckerpw.modbrowser.Curseforge;
import com.deckerpw.modbrowser.Mod;
import com.deckerpw.modbrowser.gui.component.ModSelectionList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.gui.GuiUtils;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.client.gui.ScrollPanel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Size2i;
import net.minecraftforge.jarjar.nio.pathfs.PathPath;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Stream;

public class BrowseScreen extends Screen {

    public static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
    private Screen lastScreen;
    private Curseforge cf;
    private Thread thread;
    private EditBox searchBox;
    private ModSelectionList modList;
    private int index = 0;

    public BrowseScreen(Screen lastScreen) {
        super(new TranslatableComponent("browse.name"));
        this.lastScreen = lastScreen;
    }

    public void loadMore(){
        thread.stop();
        thread = new Thread(() -> {
            try {
                this.modList.children().addAll(cf.getMods("1.18.2", searchBox.getValue(), index, 1));
                index++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }

    private void refresh(){
        thread.stop();
        index = 0;
        modList.children().clear();thread = new Thread(() -> {
            try {
                this.modList.children().addAll(cf.getMods("1.18.2", searchBox.getValue(), index, 1));
                index++;
            } catch (IOException e) {
                throw new RuntimeException(e);
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
