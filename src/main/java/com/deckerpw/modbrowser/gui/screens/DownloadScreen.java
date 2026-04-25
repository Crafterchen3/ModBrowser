package com.deckerpw.modbrowser.gui.screens;

import com.deckerpw.modbrowser.api.Modrinth;
import com.deckerpw.modbrowser.data.Mod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DownloadScreen extends Screen {

    private final Screen parent;
    private final Screen exitScreen;
    private final List<Mod> downloadList;

    private boolean finished = false;
    private Component status = Component.translatable("modbrowser.gui.downloadscreen.status.starting");

    protected DownloadScreen(Screen parent, Screen exitScreen, List<Mod> downloadList) {
        super(Component.translatable("modbrowser.gui.downloadscreen.title"));
        this.parent = parent;
        this.exitScreen = exitScreen;
        this.downloadList = downloadList;
    }

    @Override
    protected void init() {
        super.init();

        downloadMod(0);
    }

    private void downloadMod(int i){
        if (i >= downloadList.size()) {
            this.status = Component.translatable("modbrowser.gui.downloadscreen.status.complete");
            finished = true;
            return;
        }
        Mod mod = downloadList.get(i);
        status = Component.translatable("modbrowser.gui.downloadscreen.status.downloading", mod.name.getString());
        CompletableFuture<Void> future = Modrinth.downloadModAsync(mod);
        future.thenAccept((a) -> {
            assert minecraft != null;
            minecraft.execute(() -> downloadMod(i + 1));
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 10, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, this.status, this.width / 2, this.height / 2 + 10, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (finished){
            assert minecraft != null;
            minecraft.setScreen(exitScreen);
        }
    }
}
