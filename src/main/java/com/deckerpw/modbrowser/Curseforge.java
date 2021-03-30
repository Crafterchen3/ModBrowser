package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.objects.Mod;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import static com.deckerpw.modbrowser.ModBrowser.*;

public class Curseforge {

    private String searchfilter;
    private String out;
    private String version = "1.12.2";
    private String[] modArray;
    private String[] modArray2;
    private ArrayList<Mod> mods = new ArrayList<Mod>();

    public Curseforge(){
    }

    private void read() {
        try {
            File myObj = new File(NODEPATH + "Output.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                out = out + data;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();

        }
    }


    private void exec(String command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", command);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            System.out.println(line);
        }
    }

    public ArrayList<Mod> getMods(String searchfilter, String version, int page) throws IOException {
        out = "";
        mods = new ArrayList<Mod>();
        this.version = version;
        modArray = null;
        modArray2 = null;
        if(searchfilter == ""){
            this.searchfilter = "?";
        }else {
            this.searchfilter = searchfilter;
        }
        this.searchfilter = this.searchfilter.replace(" ","?");
        exec("cd " + NODEPATH + " && node GetMods.js "+version+" "+this.searchfilter+" "+page+" 20");
        read();



        modArray = out.split("<§>");
        System.out.println(modArray[0] + "LENGTH: " + modArray.length);
        for (int i = 1; i< modArray.length; i++)
        {
            modArray2 = modArray[i].split(";");
            Mod mod = new Mod();
            System.out.println(modArray2);
            mod.setName(modArray2[0]);
            mod.setId(modArray2[1]);
            mod.setAuthors(modArray2[2]);
            mod.setDescription(modArray2[3]);
            System.out.println(mod);
            mods.add(mod);
        }
        System.out.println(mods);
        return mods;

    }

    public void dowloadMod(String id) throws IOException {
        exec("cd " + NODEPATH + " && node GetFiles.js "+id+" "+version);
        exec("powershell cp "+NODEPATH+"mods\\* "+MODPATH);
        exec("powershell del "+NODEPATH+"mods\\* -r");
    }

}
