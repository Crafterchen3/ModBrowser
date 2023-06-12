package com.deckerpw.modbrowser.gui;

import com.deckerpw.modbrowser.*;
import com.deckerpw.modbrowser.gui.component.ObjectSelectionList;
import com.deckerpw.modbrowser.gui.component.tabs.TabButton;
import com.deckerpw.modbrowser.gui.component.tabs.TabManager;
import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class BrowseScreen extends Screen {

    public static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
    public static final ResourceLocation LOADING_ICON = new ResourceLocation(ModBrowser.MOD_ID,"textures/gui/loading.png");
    public Screen lastScreen;
    public Thread thread;
    public EditBox searchBox;
    public Entrys.BrowseListEntry loadingEntry;
    public ModBrowser.ModType currentModType = ModBrowser.ModType.MODS;
    public ObjectSelectionList<Entrys.BrowseListEntry> modList;
    public boolean full = false;
    public Button exitButton;

    public IModProvider mp;

    public ArrayList<File> downloadList = new ArrayList<>();
    public int index = 0;
    private final TabManager manager = new TabManager();
    private PlainTextButton plainTextButton;
    private int prohibited = 0;

    public BrowseScreen(Screen lastScreen) {
        this(lastScreen, new TranslatableComponent("browse.name"));
    }

    public BrowseScreen(Screen lastScreen, TranslatableComponent p96550) {
        super(p96550);
        this.lastScreen = lastScreen;
    }


    public ModBrowser.ModType getCurrentModType(){
        return currentModType;
    }

    private Component getExitMessage(){
        return downloadList.size() >0 ? Component.nullToEmpty(CommonComponents.GUI_DONE.getString()+ " (Download "+downloadList.size()+" files)") : CommonComponents.GUI_DONE;
    }

    public boolean existsMod(String id){
        for (File file :
                downloadList) {
            if (Objects.equals(file.mod.mod.id, id)) return true;
        }
        return false;
    }

    public void selectMod(String id){
        if (currentModType == ModBrowser.ModType.FILES){
            this.downloadList.remove(Integer.parseInt(id));

            thread.stop();
            index = 0;
            modList.children().clear();
            full = false;
            loadMore();
            exitButton.setMessage(getExitMessage());
        }
        else if (!existsMod(id)){
            try {
                ArrayList<File> files = mp.getModFiles(id);
                for (File f :
                        files) {
                    if (!existsMod(f.mod.mod.id)) downloadList.add(f);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            exitButton.setMessage(getExitMessage());
        }

    }

    public void thread(){
        if (!full) {
            this.modList.children().add(loadingEntry);
            try {
                Entrys.BrowseListEntry entry = mp.getMods(searchBox.getValue(), index, 1).get(0);
                if (!entry.mod.distribute) {
                    this.modList.children().remove(this.modList.children().size() - 1);
                    prohibited++;
                    updateProhibitStatus();
                    index++;
                    thread();
                } else {
                    if (entry.mod.id == ModBrowser.GHOST_ID) {
                        full = true;
                    }
                    if (!full || this.modList.children().size() == 1) {
                        this.modList.children().set(this.modList.children().size() - 1, entry);
                    } else this.modList.children().remove(this.modList.children().size() - 1);
                    index++;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void loadMore(){
        thread.stop();
        thread = new Thread(this::thread);
        thread.start();
    }

    public void refresh(){
        thread.stop();
        index = 0;
        modList.children().clear();
        full = false;
        this.modList.reset();
        loadMore();
    }

    @Override
    protected void init() {
        super.init();

        this.modList = new ObjectSelectionList<Entrys.BrowseListEntry>( this.minecraft, this.width, this.height, 72, this.height - 64+20, 36,this);
        TabButton button = new TabButton(manager,this.width/2-100,48,100,24,new TranslatableComponent("types.mods"),(s) ->{
            if (mp.getClass() != Curseforge.class) {
                searchBox.setEditable(true);
                mp = new Curseforge(minecraft, modList, this);
            }
            currentModType = ModBrowser.ModType.MODS;
            refresh();
        });
        this.manager.setSelected(button);
        this.addRenderableWidget(button);
        this.addRenderableWidget(new TabButton(manager,this.width/2,48,100,24,new TranslatableComponent("types.resource_packs"),(s) ->{
            if (mp.getClass() != Curseforge.class) {
                searchBox.setEditable(true);
                mp = new Curseforge(minecraft, modList, this);
            }
            currentModType = ModBrowser.ModType.RESOURCE_PACKS;
            refresh();
        }));
        this.addRenderableWidget(new TabButton(manager,this.width-100,48,100,24,new TranslatableComponent("types.list"),(s) ->{
            if (mp.getClass() != DownloadsProvider.class) {
                searchBox.setEditable(false);
                mp = new DownloadsProvider(minecraft, modList, this, downloadList);
            }
            currentModType = ModBrowser.ModType.FILES;
            refresh();
        }));
        this.addWidget(modList);
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, new TranslatableComponent("browse.search"));
        this.searchBox.setResponder((p_101362_) -> {
            refresh();
        });

        this.addWidget(this.searchBox);
        exitButton =  new Button(this.width / 2 - 100, this.height - 28, 200, 20, getExitMessage(), (p_96257_) -> {
            onClose();
        });



        thread = new Thread(() -> {
            if (!full){
                this.modList.children().add(loadingEntry);
                try {
                    Entrys.BrowseListEntry entry = mp.getMods(searchBox.getValue(), index, 1).get(0);
                    if (entry.mod.id == ModBrowser.GHOST_ID) {
                        full = true;
                    } else if (!full || this.modList.children().size() == 1) {
                        this.modList.children().set(this.modList.children().size()-1,entry);
                    }else this.modList.children().remove(this.modList.children().size()-1);
                    index++;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        this.addRenderableWidget(exitButton);

        mp = new Curseforge(minecraft,modList,this);
        this.setInitialFocus(this.searchBox);
        Mod loadingMod = new Mod();
        loadingMod.title = "Loading...";
        loadingMod.authors = "no-one";
        loadingMod.id = ModBrowser.GHOST_ID;
        loadingMod.description = "";
        loadingMod.category = "";
        this.loadingEntry = new Entrys.BrowseListEntry(minecraft,modList,this,loadingMod, false);
        refresh();
    }

    public boolean keyPressed(int p_101347_, int p_101348_, int p_101349_) {
        return super.keyPressed(p_101347_, p_101348_, p_101349_) ? true : this.searchBox.keyPressed(p_101347_, p_101348_, p_101349_);
    }

    public boolean charTyped(char p_101340_, int p_101341_) {
        return this.searchBox.charTyped(p_101340_, p_101341_);
    }

    public void updateProhibitStatus(){
        TextComponent component = new TextComponent(prohibited + " mod"+ (prohibited>1 ? "s" : "")+" can't be shown, learn More");
        int i = this.font.width(component);
        int j = width/2 - (i/2);
        if (plainTextButton != null) removeWidget(plainTextButton);
        plainTextButton = new PlainTextButton(j, this.height - 30 - 10, i, 10, component, (p_211790_) -> {
            this.minecraft.setScreen(new ConfirmLinkScreen((p_169232_) -> {
                if (p_169232_) {
                    Util.getPlatform().openUri("https://github.com/Crafterchen3/ModBrowser/wiki/Some-mods-don't-show-up.");
                }

                this.minecraft.setScreen(this);
            }, "https://github.com/Crafterchen3/ModBrowser/wiki/Some-mods-don't-show-up.", true));
        }, this.font);
        this.addRenderableWidget(plainTextButton);
    }

    @Override
    public void render(PoseStack p_96562_, int p_96563_, int p_96564_, float p_96565_) {
        renderBackground(p_96562_);
        this.modList.render(p_96562_, p_96563_, p_96564_, p_96565_);


        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F,1.0f,1.0f,1.0f);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(0,72, 0.0D).color(121,113,106,255).endVertex();
        bufferbuilder.vertex(this.width,72, 0.0D).color(121,113,106,255).endVertex();
        bufferbuilder.vertex(this.width,71, 0.0D).color(121,113,106,255).endVertex();
        bufferbuilder.vertex(0,71, 0.0D).color(121,113,106,255).endVertex();
        tesselator.end();
        RenderSystem.setShaderColor(1F,1F,1F,1F);
        super.render(p_96562_, p_96563_, p_96564_, p_96565_);
        searchBox.render(p_96562_,p_96563_,p_96564_,p_96565_);
        drawCenteredString(p_96562_, this.font, this.title, this.width / 2, 8, 16777215);

    }

    @Override
    public void onClose() {
        if (downloadList.size() > 0)
            minecraft.setScreen(new ConfirmationScreen(downloadList,this,lastScreen));
        else
            minecraft.setScreen(lastScreen);
    }


}
