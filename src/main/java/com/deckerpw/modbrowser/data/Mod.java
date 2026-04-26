package com.deckerpw.modbrowser.data;

import com.deckerpw.modrinth.data.ProjectType;
import com.deckerpw.modrinth.data.Version;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;

public class Mod {

    public final String id;
    public final String slug;
    public final BufferedImage icon;
    public final String author;
    public final Component name;
    public final Component summary;
    public final ProjectType type;
    @Nullable
    public Version version;

    public Mod(String id, String slug, BufferedImage icon, String author, Component name, Component summary, ProjectType type) {
        this.id = id;
        this.slug = slug;
        this.icon = icon;
        this.author = author;
        this.name = name;
        this.summary = summary;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Mod mod && mod.id.equals(this.id);
    }
}
