package com.deckerpw.modbrowser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface IModProvider {


    List<Entrys.BrowseListEntry> getMods(String searchFilter, int page, int pageSize) throws IOException ;

    ArrayList<File> getModFiles(Mod mod) throws IOException;

    File getModFile(String id) throws IOException;
}
