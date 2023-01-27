package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.gui.component.ModSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.json.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.deckerpw.modbrowser.ModBrowser.*;

public class Curseforge {

    private String base_url = "https://api.curseforge.com/v1/mods/";
    private String gameMeta = "?gameId=432&classId=6&modLoaderType=1";
    private Minecraft mc;
    private ModSelectionList modSelectionList;
    private Screen screen;

    public Curseforge(Minecraft mc, ModSelectionList modSelectionList, Screen screen) {
        this.mc = mc;
        this.modSelectionList = modSelectionList;
        this.screen = screen;
    }

    private String apiKey = "$2a$10$ovhZip0zfjpEgB7p2z4TSuKh2861OLVcAeGaKRZlo5jWk6MjRTnu6";

    private String readURL(URL mURL) throws IOException {
        HttpURLConnection con = (HttpURLConnection) mURL.openConnection();
        con.setRequestMethod("GET");
        con.addRequestProperty("x-api-key",apiKey);
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public ArrayList<File> getDependencies(int identifier) throws IOException {
        JSONObject file = getModFilesJSON(identifier);
        ArrayList<File> dependencies = new ArrayList<File>();
        if (file.has("dependencies")){
            JSONArray dependenciesJSON = file.getJSONArray("dependencies");
            for (int i = 0; i < dependenciesJSON.length(); i++) {
                JSONObject dependency = dependenciesJSON.getJSONObject(i);
                if (dependency.getInt("relationType") == 3){

                    dependencies.add(getModFile(dependency.getInt("modId")));
                    dependencies.addAll(getDependencies(dependency.getInt("modId")));
                }
            }
        }
        return dependencies;
    }


    public void downloadFile(File file) throws IOException {
        InputStream in = new URL(file.downloadUrl).openStream();
        Files.copy(in, Paths.get(MODPATH,file.fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    public void downloadModAndDependencies(int id) throws IOException {
        if (id != GHOST_ID){
            ArrayList<File> downloads = getDependencies(id);
            downloads.add( getModFile(id));
            for (File file : downloads) {
                downloadFile(file);
            }
        }

    }

    private ModSelectionList.ModListEntry jsonToMod(JSONObject obj) throws IOException {
        Mod mod = new Mod();
        mod.id = obj.getInt("id");
        mod.title = obj.optString("name");
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
        mod.category = getCategory(obj);
        mod.description = obj.getString("summary");
        mod.logoURL = obj.getJSONObject("logo").getString("url");
        return new ModSelectionList.ModListEntry(this.mc,this.modSelectionList,this.screen,mod);
    }

    private File jsonToFile(JSONObject obj,int modId) throws IOException {
        File file = new File();
        file.id = obj.getInt("id");
        file.mod = getMod(modId);
        file.fileName = obj.getString("fileName");
        file.downloadUrl = obj.getString("downloadUrl");
        ArrayList<String> gameVersions = new ArrayList<>();
        JSONArray arr = obj.getJSONArray("gameVersions");
        for(int i=0;i<arr.length();i++){
            gameVersions.add(arr.getString(i));
        }
        file.gameVersions = gameVersions;
        return file;
    }

    public String getCategory(JSONObject obj){
        int primary = obj.getInt("primaryCategoryId");
        JSONArray categories = obj.getJSONArray("categories");
        for (int i = 0; i < categories.length(); i++) {
            JSONObject category = categories.getJSONObject(i);
            if(category.getInt("id") == primary){
                return category.getString("name");
            }
        }
        return "";
    }

    public List<ModSelectionList.ModListEntry> getMods( String searchFilter, int page,int pageSize) throws IOException {
        searchFilter = searchFilter.replaceAll(" ","%20");
        String searchURL = base_url+"search"+gameMeta+"&sortField="+1+"&sortOrder="+"desc"+"&pageSize="+pageSize+"&gameVersion="+ MC_VERSION+"&index="+page+"&searchFilter="+searchFilter;
        //TODO add Config:
        // String searchURL = base_url+"search"+gameMeta+"&sortField="+ModBrowserConfig.sortType.value+"&sortOrder="+ModBrowserConfig.sortOrder.value+"&pageSize=20&gameVersion="+gameVersion+"&index="+page+"&searchFilter="+searchFilter;
        //System.out.println(searchURL);
        String result = readURL(new URL(searchURL));
        JSONArray jsonArray = new JSONObject(result).getJSONArray("data");
        List<ModSelectionList.ModListEntry> mods = new ArrayList<>();
        for (int i=0;i<jsonArray.length();i++){
            mods.add(jsonToMod(jsonArray.getJSONObject(i)));
        }
        if (mods.size() == 0){
            Mod mod = new Mod();
            mod.category = "";
            mod.id = GHOST_ID; //a special code so you won't be able to download
            mod.authors = "no-one";
            mod.title = "No search results!";
            mod.description = "Curseforge couldn't find any mods for this search";

            mods.add(new ModSelectionList.ModListEntry(mc,modSelectionList,screen,mod));
        }

        return mods;
    }

    public File getModFile(int identifier) throws IOException {
        String searchURL = base_url+identifier+"/files?gameVersion="+MC_VERSION+"&modLoaderType=1";
        //System.out.println(searchURL);
        String result = readURL(new URL(searchURL));
        JSONArray files = new JSONObject(result).getJSONArray("data");
        return jsonToFile(files.getJSONObject(0),identifier);

    }

    private JSONObject getModFilesJSON(int identifier) throws IOException {
        JSONObject jsonObject = null;
        String searchURL = base_url+identifier+"/files?gameVersion="+MC_VERSION+"&modLoaderType=1";
        //System.out.println(searchURL);
        String result = readURL(new URL(searchURL));
        JSONArray files = new JSONObject(result).getJSONArray("data");


        return files.getJSONObject(0);
    }


    public ModSelectionList.ModListEntry getMod(int identifier) throws IOException {
        String searchURL = base_url+identifier;
        //System.out.println(searchURL);
        String result = readURL(new URL(searchURL));
        JSONObject obj = new JSONObject(result).getJSONObject("data");
        ModSelectionList.ModListEntry mod = jsonToMod(obj);
        return mod;
    }


    private JSONObject parse(String str){
        JSONObject obj = new JSONObject(str);
        return obj;
    }








}
