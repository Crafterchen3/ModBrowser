package com.deckerpw.modbrowser.gui;

import com.deckerpw.modbrowser.File;
import com.deckerpw.modbrowser.IModProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.client.event.RenderTooltipEvent;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DownloadScreen extends Screen {

    private boolean started = false;
    private String currentMod;
    private ArrayList<File> finished = new ArrayList<>();
    private final ArrayList<File> downloads;
    private final Screen main;
    private boolean done = false;
    private int currentProgress = 0;

    protected DownloadScreen(ArrayList<File> downloads, Screen main) {
        super(new TranslatableComponent("download.name"));
        this.downloads = downloads;
        this.main = main;
    }

    @Override
    protected void init() {
        super.init();
        Thread thread = new Thread(() -> {
            started = true;
            for (File file:
                 downloads) {
                currentMod = file.mod.mod.title;
                currentProgress = 0;
                try {
                    URL url = new URL(file.downloadUrl);
                    HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
                    long completeFileSize = httpConnection.getContentLength();

                    java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(Paths.get(minecraft.gameDirectory.getPath() + file.mod.mod.modType.prefix, file.fileName).toFile());
                    java.io.BufferedOutputStream bout = new BufferedOutputStream(
                            fos, 1024);
                    byte[] data = new byte[1024];
                    long downloadedFileSize = 0;
                    int x = 0;
                    while ((x = in.read(data, 0, 1024)) >= 0) {
                        downloadedFileSize += x;
                        // calculate progress
                        currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize)) * 100d);
                        bout.write(data, 0, x);
                    }
                    bout.close();
                    in.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                finished.add(file);
            }
            done = true;
        });
        thread.start();
    }

    @Override
    public void render(PoseStack p_96562_, int p_96563_, int p_96564_, float p_96565_) {
        this.renderBackground(p_96562_);
        super.render(p_96562_, p_96563_, p_96564_, p_96565_);
        float l = 300f/downloads.size();
        float d = finished.size();
        float f = (l/100f)*currentProgress;
        float k = l*d+f;
        drawCenteredString(p_96562_,font,new TranslatableComponent("download.total",finished.size(),downloads.size()),width/2,5,16777215);
        fill(p_96562_,width/2-151,19,width/2+151,31, -16777216);
        fill(p_96562_,width/2-150,20,width/2-150+(int)k,30, -38400);
        l = 300f/100f;
        k = l*currentProgress;
        fill(p_96562_,width/2-151,height/2-6,width/2+151,height/2+6, -16777216);
        fill(p_96562_,width/2-150,height/2-5,width/2-150+(int)k,height/2+5, -38400);
        drawCenteredString(p_96562_,font,currentProgress+"%",width/2,height/2-5,10526880);
        drawCenteredString(p_96562_,minecraft.font,started ? new TranslatableComponent("download.instr",currentMod) : new TranslatableComponent("download.init"),this.width / 2, this.height/2-50, 16777215);
    }

    @Override
    public void tick() {
        super.tick();
        if (done)
            minecraft.setScreen(main);
    }
}
