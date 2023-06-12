package com.deckerpw.modbrowser;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class File {

    public int id;
    public Entrys.BrowseListEntry mod;
    public String fileName;
    public String downloadUrl;
    public ArrayList<String> gameVersions;
    public double fileSize;

    public File(){}

    public void download(Minecraft mc)throws IOException {
        InputStream in = new URL(downloadUrl).openStream();
        Files.copy(in, Paths.get(mc.gameDirectory.getPath() + mod.mod.modType.prefix, fileName), StandardCopyOption.REPLACE_EXISTING);
    }

}
