package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.gui.BrowseScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;

public class Mod implements PackSelectionModel.Entry {

    public int id;
    public String title;
    public String description;
    public boolean last = true;

    public String logoURL;

    public String category;
    public String authors;
    public boolean selected = false;

    public Mod() {}

    @Override
    public ResourceLocation getIconTexture() {
        return BrowseScreen.DEFAULT_ICON;
    }

    @Override
    public PackCompatibility getCompatibility() {
        return PackCompatibility.COMPATIBLE;
    }
    public String getLogoURL() {
        return logoURL;
    }
    @Override
    public Component getTitle() {
        return Component.nullToEmpty(title);
    }

    @Override
    public Component getDescription() {
        return Component.nullToEmpty(description);
    }
    public String getCategory() {
        return category;
    }

    @Override
    public PackSource getPackSource() {
        return null;
    }

    @Override
    public boolean isFixedPosition() {
        return false;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public void select() {
        selected = true;
    }

    @Override
    public void unselect() {
        selected = false;
    }

    @Override
    public void moveUp() {

    }

    @Override
    public void moveDown() {

    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean canMoveUp() {
        return false;
    }

    @Override
    public boolean canMoveDown() {
        return false;
    }

    @Override
    public boolean canSelect() {
        return true;
    }

    @Override
    public boolean canUnselect() {
        return true;
    }

    @Override
    public Component getExtendedDescription() {
        return getDescription();
    }
}
