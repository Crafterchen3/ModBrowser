package com.deckerpw.modbrowser.gui.component.tabs;

import java.util.ArrayList;

public class TabManager {

    private TabButton selected;

    public TabManager(){}

    public TabButton getSelected(){
        return selected;
    }

    public void setSelected(TabButton i){
        selected = i;
    }

}
