package com.deckerpw.modbrowser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

public class AutoUpdate {

    private final Minecraft mc;
    private static boolean started = false;

    public AutoUpdate(Minecraft mc) {
        this.mc = mc;
    }

    public void updateMods(){
        AutoUpdate.started = true;
        Curseforge curseforge = new Curseforge(Minecraft.getInstance());
        Thread thread1 = new Thread(() -> {
            Object[] mods = ModBrowser.index.modsIndex.keySet().toArray();
            Object[] resourcepacks = ModBrowser.index.resourcepacksIndex.keySet().toArray();
            try {
                ArrayList<File> results = new ArrayList<>();
                ArrayList<File> files = new ArrayList<>();
                for (Object id :
                        mods) {
                    File file = curseforge.getModFileNoScreen(Integer.parseInt(id.toString()), ModBrowser.ModType.MODS);
                    file.index = ModBrowser.index.getIndex(Integer.parseInt(id.toString()));
                    files.add(file);
                }
                for (Object id :
                        resourcepacks) {
                    File file = curseforge.getModFileNoScreen(Integer.parseInt(id.toString()), ModBrowser.ModType.RESOURCE_PACKS);
                    file.index = ModBrowser.index.getIndex(Integer.parseInt(id.toString()));
                    files.add(file);
                }
                results.addAll(files);
                ArrayList<File> filter = new ArrayList<>();
                for (File file :
                        files) {
                    if (file.fileName.equals(file.index.fileName))
                    {
                        filter.add(file);
                    }
                }
                results.removeAll(filter);
                ToastComponent toasts = mc.getToasts();
                if (results.size() < 1) {
                    toasts.addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, new TranslatableComponent("browse.toast.up-to-date.title"), new TranslatableComponent("browse.toast.up-to-date.subtitle", files.size())));
                }else if (ModBrowser.ModBrowserConfigs.AUTO_UPDATE_BEHAVIOR.get() == AutoUpdateBehavior.AUTO_INFORM) {
                    toasts.addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, new TranslatableComponent("browse.toast.update_found.title"), new TranslatableComponent("browse.toast.update_found.subtitle", results.size())));
                }else {
                    for (File file: results) {
                        if (ModBrowser.index.isModInstalled(file.index.id)){
                            java.io.File temp = Paths.get(mc.gameDirectory.getPath() + file.index.prefix, file.index.fileName).toFile();
                            if (temp.exists())
                                temp.deleteOnExit();
                        }
                        try {
                            URL url = new URL(file.downloadUrl);
                            HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
                            long completeFileSize = httpConnection.getContentLength();

                            java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
                            java.io.FileOutputStream fos = new java.io.FileOutputStream(Paths.get(mc.gameDirectory.getPath() + file.index.prefix, file.fileName).toFile());
                            java.io.BufferedOutputStream bout = new BufferedOutputStream(
                                    fos, 1024);
                            byte[] data = new byte[1024];
                            long downloadedFileSize = 0;
                            int x = 0;
                            while ((x = in.read(data, 0, 1024)) >= 0) {
                                downloadedFileSize += x;
                                bout.write(data, 0, x);
                            }
                            bout.close();
                            in.close();
                            file.index.fileName = file.fileName;
                            ModBrowser.index.setModIndex(file.index);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    toasts.addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, new TranslatableComponent("browse.toast.update_installed.title"), new TranslatableComponent("browse.toast.update_installed.subtitle", results.size())));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread1.start();
    }

    public enum AutoUpdateBehavior{
        NO_AUTO_UPDATE(),
        AUTO_INFORM(),
        AUTO_INSTALL();

        AutoUpdateBehavior(){

        }
    }

}
