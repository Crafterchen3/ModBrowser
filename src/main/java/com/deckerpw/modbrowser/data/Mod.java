package com.deckerpw.modbrowser.data;

import com.deckerpw.modrinth.data.ProjectType;
import com.deckerpw.modrinth.data.Version;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Mod {

    public final String id;
    public final String slug;
    @Nullable
    public DynamicTexture icon = null;
    public final String author;
    public final Component name;
    public final Component summary;
    public final ProjectType type;
    @Nullable
    public Version version;

    public Mod(String id, String slug, BufferedImage iconImage, String author, Component name, Component summary, ProjectType type) {
        this.id = id;
        this.slug = slug;
        this.author = author;
        this.name = name;
        this.summary = summary;
        this.type = type;

        Minecraft.getInstance().execute(() -> loadIcon(iconImage));
    }

    private void loadIcon(BufferedImage iconImage) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(iconImage, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            icon = new DynamicTexture(NativeImage.read(is));
            icon.upload();
        } catch (IOException ignored) {
            icon = null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Mod mod && mod.id.equals(this.id);
    }
}
