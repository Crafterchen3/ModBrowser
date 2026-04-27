package com.deckerpw.modbrowser.data;

import com.deckerpw.modbrowser.api.ModType;
import com.deckerpw.modbrowser.gui.components.ModSelectionList;
import net.minecraft.network.chat.Component;

public class TabDefinition {

    public final ModType type;
    public final Component title;
    public ModSelectionList list;
    public boolean moreAvailable = true;
    public int nextOffset;
    public boolean loading;

    public TabDefinition(ModType type, Component title) {
        this.type = type;
        this.title = title;
    }
}
