package com.deckerpw.modbrowser.objects;

import com.deckerpw.modbrowser.Curseforge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
public class File {

    public int id;
    public Mod mod;
    public String fileName;
    public String downloadUrl;
    public ArrayList<String> gameVersions;

    public File(){}


}
