package com.deckerpw.modbrowser;


import net.minecraft.client.Minecraft;
import org.json.JSONObject;

import java.io.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//This class holds, loads and saves the current installed mods
public class ModIndex {

    private final String gamedir;
    private final Path indexFilePath;

    public JSONObject modsIndex;
    public JSONObject resourcepacksIndex;

    public ModIndex(Minecraft mc) throws IOException {
        gamedir = mc.gameDirectory.toString();
        this.indexFilePath = Paths.get(gamedir,"index.json");
        load();
    }

    private JSONObject fileToJSONObject(Path file) throws IOException {
        // Open the file using a FileReader
        FileReader fileReader = new FileReader(file.toString());

        // Wrap the FileReader in a BufferedReader for efficient reading
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        // Read each line of the file and append it to the StringBuilder
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        // Close the BufferedReader and FileReader
        bufferedReader.close();
        fileReader.close();

        // Parse the content as a JSONObject
        String fileContent = stringBuilder.toString();
        JSONObject jsonObject = new JSONObject(fileContent);
        return jsonObject;
    }
    public boolean pathExists(Path filePath){
        return new File(filePath.toString()).exists();
    }

    public void load() throws IOException {
        if (pathExists(indexFilePath)) {
            JSONObject index = fileToJSONObject(indexFilePath);
            modsIndex = index.getJSONObject("/mods");
            resourcepacksIndex = index.getJSONObject("/resourcepacks");
            check();
            save();
        } else {
            modsIndex = new JSONObject();
            resourcepacksIndex = new JSONObject();
            save(); // Create initial File
        }
    }

    public void save() throws IOException {
        // Create a FileWriter to write to the file
        FileWriter fileWriter = new FileWriter(indexFilePath.toString());
        JSONObject index = new JSONObject();
        index.put("/mods", modsIndex);
        index.put("/resourcepacks",resourcepacksIndex);
        // Write the JSON object to the file
        fileWriter.write(index.toString(4)); // Use toString(4) for pretty-printing (optional)

        // Close the FileWriter
        fileWriter.close();
    }

    private boolean containsFileName(List<File> list,String filename){
        for (File file:
             list) {
            if (file.getName().equals(filename))
                return true;
        }
        return false;
    }

    private JSONObject internalCheck(String path,JSONObject index){
        File folder = new File(path);
        Map<String,Object> map = index.toMap();
        File[] elements = folder.listFiles();
        if (elements != null)
        {
            List<File> list = List.of(elements);
            map.forEach((key, value) -> {
                if (!containsFileName(list, (String) value))
                {
                    index.remove(key);
                }
            });
            return index;
        }
        return new JSONObject();

    }

    public void check() {
        modsIndex = internalCheck(gamedir+"/mods",modsIndex);
        resourcepacksIndex = internalCheck(gamedir+"/resourcepacks",resourcepacksIndex);
    }

    public boolean isModInstalled(int id){
        return modsIndex.has(Integer.toString(id)) || resourcepacksIndex.has(Integer.toString(id));
    }

    public FileIndex getIndex(int id){
        String sID = Integer.toString(id);
        if (modsIndex.has(sID))
            return new FileIndex(sID, modsIndex.getString(sID),"/mods");
        else
            return new FileIndex(sID, resourcepacksIndex.getString(sID),"/resourcepacks");
    }

    public void setModIndex(FileIndex index) {
        if (index.prefix == "/mods")
            modsIndex.put(Integer.toString(index.id),index.fileName);
        else
            resourcepacksIndex.put(Integer.toString(index.id),index.fileName);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class FileIndex{

        public int id;

        protected String sID;
        public String fileName;
        public String prefix;

        public FileIndex(int id, String fileName, String prefix) {
            this.id = id;
            this.fileName = fileName;
            this.prefix = prefix;
        }

        protected FileIndex(String id, String fileName, String prefix) {
            this.id = Integer.parseInt(id);
            this.sID = id;
            this.fileName = fileName;
            this.prefix = prefix;
        }

    }

}
