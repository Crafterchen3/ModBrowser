package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.objects.Mod;
import org.json.*;

import java.io.*;
import java.net.URL;
import com.deckerpw.modbrowser.objects.File;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;

import static com.deckerpw.modbrowser.ModBrowser.*;

public class Curseforge {

    private String base_url = "https://addons-ecs.forgesvc.net/api/v2/addon/";
    private JSONArray jsonArray;

    private String readURL(String mURL) throws IOException {
        String out = new Scanner(new URL(mURL).openStream(), "UTF-8").useDelimiter("\\A").next();
        return out;
    }

    public void downloadFile(File file) throws IOException {
        InputStream in = new URL(file.downloadUrl).openStream();
        Files.copy(in, Paths.get(MODPATH,file.fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    private Mod jsonToMod(JSONObject obj){
        Mod mod = new Mod();
        mod.id = obj.getInt("id");
        mod.name = obj.optString("name");
        JSONArray arr = obj.getJSONArray("authors");
        String result = "";
        for(int i=0;i < arr.length();i++){
            String str = arr.getJSONObject(i).getString("name");
            if (i==0){
                result += str;
            }else{
                result += (", "+str);
            }
        }
        mod.authors = result;
        mod.description = obj.getString("summary");
        return mod;
    }

    private File jsonToFile(JSONObject obj){
        File file = new File();
        file.id = obj.getInt("id");
        file.fileName = obj.getString("fileName");
        file.downloadUrl = obj.getString("downloadUrl");
        ArrayList<String> gameVersions = new ArrayList<>();
        JSONArray arr = obj.getJSONArray("gameVersion");
        for(int i=0;i<arr.length();i++){
            gameVersions.add(arr.getString(i));
        }
        file.gameVersions = gameVersions;
        return file;
    }


    public ArrayList<Mod> getMods(String gameVersion,String searchFilter,int page) throws IOException {
        searchFilter.replaceAll(" ","%20");
        String searchURL = base_url+"search?gameId=432&sectionId=6&pageSize=20&gameVersion="+gameVersion+"&index="+page+"&searchFilter="+searchFilter;
        System.out.println(searchURL);
        String result = readURL(searchURL);
        JSONArray jsonArray = new JSONArray(result);
        ArrayList<Mod> mods = new ArrayList<>();
        for (int i=0;i<jsonArray.length();i++){
            mods.add(jsonToMod(jsonArray.getJSONObject(i)));
        }
        if (mods.size() == 0){
            Mod mod = new Mod();
            mod.description = "Unfortunately no Mod has been found";
            mod.id = -54; //a special code for not being able to download
            mod.authors = "):";
            mod.name = "Sorry";
            mods.add(mod);
        }
        return mods;
    }

    public ArrayList<File> getModFiles(int identifier) throws IOException {
        String searchURL = base_url+identifier+"/files";
        System.out.println(searchURL);
        String result = readURL(searchURL);
        jsonArray = new JSONArray(result);
        ArrayList<File> files = new ArrayList<>();
        for (int i=0;i<jsonArray.length();i++){
            File file = jsonToFile(jsonArray.getJSONObject(i));
            if (file.gameVersions.contains("1.12.2")){
                files.add(file);
            }
        }

        return files;
    }

    public Mod getMod(int identifier) throws IOException {
        String searchURL = base_url+identifier;
        System.out.println(searchURL);
        String result = readURL(searchURL);
        JSONObject obj = new JSONObject(result);
        Mod mod = jsonToMod(obj);
        return mod;
    }


    private JSONObject parse(String str){
        JSONObject obj = new JSONObject(str);
        return obj;
    }


    public void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("IT WORKS!!");
        ArrayList<Mod> mods = getMods("1.12.2","Immersive",0);
        System.out.println(mods);
        System.out.println(jsonArray);
        Mod mod = getMod(mods.get(0).id);
        System.out.println(mod);
        ArrayList<File> files = getModFiles(mod.id);
        System.out.println(files);
    }






}
