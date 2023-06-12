package com.deckerpw.modbrowser.gui;

import com.deckerpw.modbrowser.File;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;

public class ConfirmationScreen extends Screen {

    final ArrayList<File> downloads;
    final Screen last;
    final Screen main;
    private double totalSize;
    private Button cancelButton;
    private Button exitButton;
    private Button continueButton;

    public ConfirmationScreen(ArrayList<File> downloads, Screen last,Screen main) {
        super(new TranslatableComponent("confirm.name"));
        this.downloads = downloads;
        this.last = last;
        this.main = main;
        for (File f :
                downloads) {
            totalSize += f.fileSize;
        }
        totalSize = Math.round(totalSize * 10.0) / 10.0;
    }

    @Override
    protected void init() {
        super.init();
        cancelButton =  new Button(this.width / 2 - 50 - 5 - 100, this.height - 28, 100, 20, new TranslatableComponent("confirm.button.cancel"), (p_96257_) -> {
            onClose();
        });
        this.addRenderableWidget(cancelButton);
        continueButton =  new Button(this.width / 2 - 50, this.height - 28, 100, 20, new TranslatableComponent("confirm.button.continue"), (p_96257_) -> {
            this.minecraft.setScreen(new DownloadScreen(downloads,main));
        });
        this.addRenderableWidget(continueButton);
        exitButton =  new Button(this.width / 2 + 50 + 5, this.height - 28, 100, 20, new TranslatableComponent("confirm.button.exit"), (p_96257_) -> {
            this.minecraft.setScreen(main);
        });
        this.addRenderableWidget(exitButton);

    }

    @Override
    public void render(PoseStack poseStack, int i, int i1, float v) {
        renderDirtBackground(i);
        super.render(poseStack, i, i1, v);
        drawCenteredString(poseStack,font,new TranslatableComponent("confirm.total.files",downloads.size()),width/2,height/2-10,0xFFFFFFFF);
        drawCenteredString(poseStack,font,new TranslatableComponent("confirm.total.size",totalSize),width/2,height/2+10,0xFFFFFFFF);


    }

    @Override
    public void onClose() {
        minecraft.setScreen(last);
    }
}
