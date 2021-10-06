package com.deckerpw.modbrowser.objects;

import com.deckerpw.modbrowser.Curseforge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
public class File {

    public int id;
    public String fileName;
    public String downloadUrl;
    public ArrayList<String> gameVersions;
    private ArrayList<Mod> dependencies;

    public File(){}

    public ArrayList<Mod> getDependencies() {
        return dependencies;
    }

    public void setDependencies(JSONArray jsonArray,Curseforge curseforge) throws IOException {
        ArrayList<Mod> dependencies = new ArrayList<>();
        for(int i=0;i < jsonArray.length();i++){
            JSONObject obj = jsonArray.getJSONObject(0);
            int id = obj.getInt("addonId");
            Mod mod = curseforge.getMod(id);
            Mod mod1 = curseforge.getMod(this.id);
            mod.authors = "required by: "+mod1.name;
            dependencies.add(mod);
        }
        this.dependencies = dependencies;
    }
}
