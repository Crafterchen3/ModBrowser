package com.deckerpw.modbrowser.gui.screens;

import com.deckerpw.modbrowser.data.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ConfirmDownloadScreen extends ConfirmScreen {

    private final Screen parent;
    private final Screen exitScreen;

    public ConfirmDownloadScreen(Screen parent, Screen exitScreen, List<Mod> downloadList) {
        super(t -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(t ? new DownloadScreen(parent, exitScreen, downloadList) : parent);
        }, Component.translatable("modbrowser.gui.confirmDownload.title"), Component.translatable("modbrowser.gui.confirmDownload.message", downloadList.size()), Component.translatable("modbrowser.gui.confirmDownload.buttons.yes"), Component.translatable("modbrowser.gui.confirmDownload.buttons.no"));

        this.parent = parent;
        this.exitScreen = exitScreen;
    }


    @Override
    protected void addButtons(int y) {
        this.addRenderableWidget(
                Button.builder(this.yesButton, button -> this.callback.accept(true)).bounds(this.width / 2 - 50 - 105, y, 100, 20).build()
        );
        this.addRenderableWidget(Button.builder(Component.translatable("modbrowser.gui.confirmDownload.buttons.exit"), button -> {
            minecraft.setScreen(this.exitScreen);
        }).bounds(this.width / 2 - 50, y, 100, 20).build());
        this.addRenderableWidget(
                Button.builder(this.noButton, button -> this.callback.accept(false)).bounds(this.width / 2 - 50 + 105, y, 100, 20).build()
        );
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

}
