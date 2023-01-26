package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.Curseforge;
import com.deckerpw.modbrowser.gui.component.ModSelectionList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
public class File {

    public int id;
    public ModSelectionList.ModListEntry mod;
    public String fileName;
    public String downloadUrl;
    public ArrayList<String> gameVersions;

    public File(){}


}
