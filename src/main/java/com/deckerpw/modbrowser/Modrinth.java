package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.gui.BrowseScreen;
import com.deckerpw.modbrowser.gui.component.ObjectSelectionList;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.deckerpw.modbrowser.ModBrowser.GHOST_ID;

public class Modrinth implements IModProvider {

    private final String baseUrl = "https://api.modrinth.com/v2/";
    private final Minecraft mc;
    private final ObjectSelectionList<Entrys.BrowseListEntry> modSelectionList;
    private final BrowseScreen screen;

    public Modrinth(Minecraft mc, ObjectSelectionList<Entrys.BrowseListEntry> modSelectionList, BrowseScreen screen) {
        this.mc = mc;
        this.modSelectionList = modSelectionList;
        this.screen = screen;
    }

    private static String readResponse(String url) throws IOException {
        URL apiUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        InputStream is = conn.getInputStream();
        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        is.close();
        conn.disconnect();
        return response;
    }

    @Override
    public List<Entrys.BrowseListEntry> getMods(String searchFilter, int page, int pageSize) throws IOException {
        String url = "https://api.modrinth.com/v2/search?facets="
                + encodeQuery("[[\"categories:forge\"],[\"versions:1.18.2\"],[\"project_type:mod\"]]")
                + "&query="
                + encodeQuery(searchFilter)
                + "&limit="
                + pageSize
                + "&offset="
                + page*pageSize;
        String response = readResponse(url);
        JSONObject rootObj = new JSONObject(response);
        JSONArray hits = rootObj.getJSONArray("hits");
        List<Entrys.BrowseListEntry> mods = new ArrayList<>();
        for (int i = 0; i < hits.length(); i++) {
            JSONObject proj = hits.getJSONObject(i);

            Mod mod = new Mod();
            mod.id = proj.getString("project-id");
            mod.title = proj.getString("title");
            mod.description = proj.getString("description");
            mod.modType = ModBrowser.ModType.MODS;
            mod.category = proj.getJSONArray("display_categories").join(", ");
            mod.authors = proj.getString("author");
            HttpURLConnection con = (HttpURLConnection) new URL(proj.getString("icon_url")).openConnection();
            mod.logoURL = new DynamicTexture(NativeImage.read(con.getInputStream()));

            mods.add(new Entrys.BrowseListEntry(mc, modSelectionList, screen, mod));
        }
        if (mods.isEmpty()){
            Mod mod = new Mod();
            mod.category = "";
            mod.id = "";
            mod.authors = "?";
            mod.title = "No search results!";
            mod.description = "Curseforge couldn't find any mods for this search";

            mods.add(new Entrys.BrowseListEntry(mc, modSelectionList, screen, mod));
        }
        return mods;
    }

    private String encodeQuery(String string){
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    @Override
    public ArrayList<File> getModFiles(Mod mod) throws IOException {
        return null;
    }

    @Override
    public File getModFile(String id) throws IOException {
        return null;
    }
}
