package com.deckerpw.modbrowser.gui;

import com.deckerpw.modbrowser.Curseforge;
import com.deckerpw.modbrowser.File;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;
import java.util.ArrayList;

public class DownloadScreen extends Screen {

    private boolean started = false;
    private String currentMod;
    private final Curseforge cf;
    private final ArrayList<File> downloads;
    private final Screen main;
    private boolean done = false;

    protected DownloadScreen(ArrayList<File> downloads, Screen main,Curseforge cf) {
        super(new TranslatableComponent("download.name"));
        this.downloads = downloads;
        this.main = main;
        this.cf = cf;
    }

    @Override
    protected void init() {
        super.init();
        Thread thread = new Thread(() -> {
            started = true;
            for (File file:
                 downloads) {
                currentMod = file.mod.mod.title;
                try {
                    cf.downloadFile(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            done = true;
        });
        thread.start();
    }

    @Override
    public void render(PoseStack p_96562_, int p_96563_, int p_96564_, float p_96565_) {
        this.renderBackground(p_96562_);
        super.render(p_96562_, p_96563_, p_96564_, p_96565_);
        drawCenteredString(p_96562_,minecraft.font,started ? new TranslatableComponent("download.instr",currentMod) : new TranslatableComponent("download.init"),this.width / 2, this.height/2, 16777215);
    }

    @Override
    public void tick() {
        super.tick();
        if (done)
            minecraft.setScreen(main);
    }
}
