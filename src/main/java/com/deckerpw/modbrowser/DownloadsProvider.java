package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.gui.BrowseScreen;
import com.deckerpw.modbrowser.gui.component.ObjectSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.deckerpw.modbrowser.ModBrowser.GHOST_ID;

public class DownloadsProvider implements IModProvider{

    private final ArrayList<File> downloadList;
    private Minecraft mc;
    private ObjectSelectionList<Entrys.BrowseListEntry> modSelectionList;
    private BrowseScreen screen;

    public DownloadsProvider(Minecraft mc, ObjectSelectionList<Entrys.BrowseListEntry> modSelectionList, BrowseScreen screen,ArrayList<File> downloads) {
        this.mc = mc;
        this.modSelectionList = modSelectionList;
        this.screen = screen;
        this.downloadList = downloads;
    }

    @Override
    public List<Entrys.BrowseListEntry> getMods(String searchFilter, int page, int pageSize) throws IOException {
        ArrayList<Entrys.BrowseListEntry> list = new ArrayList<>();
        try {
            Mod smod = screen.downloadList.get(page).mod.mod;
            Mod mod = new Mod();
            mod.id = ""+page;
            mod.logo = smod.logo;
            mod.title = smod.title;
            mod.category = smod.category;
            mod.authors = smod.authors;
            mod.logoURL = smod.logoURL;
            mod.description = smod.description;
            mod.modType = ModBrowser.ModType.FILES;
            list.add(new Entrys.BrowseListEntry(mc,modSelectionList,screen,mod));
        }catch (IndexOutOfBoundsException e){
            Mod mod = new Mod();
            mod.category = "";
            mod.id = GHOST_ID; //a special code so you won't be able to download
            mod.authors = "no-one";
            mod.title = "No search results!";
            mod.description = "Curseforge couldn't find any mods for this search";

            list.add(new Entrys.BrowseListEntry(mc, modSelectionList, screen, mod));
        }
        return list;
    }

    @Override
    public ArrayList<File> getModFiles(String identifier) throws IOException {
        return null;
    }
}
