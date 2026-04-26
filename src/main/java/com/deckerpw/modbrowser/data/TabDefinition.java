package com.deckerpw.modbrowser.data;

import com.deckerpw.modbrowser.gui.components.ModSelectionList;
import com.deckerpw.modrinth.data.ProjectType;
import net.minecraft.network.chat.Component;

public class TabDefinition {

    public final ProjectType type;
    public final Component title;
    public ModSelectionList list;
    public boolean moreAvailable = true;
    public int nextOffset;
    public boolean loading;

    public TabDefinition(ProjectType type, Component title) {
        this.type = type;
        this.title = title;
    }
}
