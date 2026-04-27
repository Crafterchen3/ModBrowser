package com.deckerpw.modbrowser.gui.screens;

import com.deckerpw.modbrowser.api.ModType;
import com.deckerpw.modbrowser.api.Modrinth;
import com.deckerpw.modbrowser.data.Mod;
import com.deckerpw.modbrowser.data.TabDefinition;
import com.deckerpw.modbrowser.gui.components.ModSelectionList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class BrowseScreen extends Screen {
    private static final int PAGE_SIZE = 1;
    private static final ResourceLocation TAB_HEADER_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/tab_header_background.png");
    private static final Field TAB_BAR_LAYOUT_FIELD = findTabBarLayoutField();
    private final AtomicLong searchRequestId = new AtomicLong();
    private final Screen parent;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private final List<Mod> downloadList = new ArrayList<>();
    private final TabDefinition[] tabDefinitions;
    private long activeSearchGeneration;
    @NotNull
    private String activeSearchQuery = "";
    @Nullable
    private EditBox searchBox;
    @Nullable
    private ModSelectionList downloadListView;
    @Nullable
    private TabNavigationBar tabNavigationBar;
    private Component status = Component.empty();
    private Button exitButton;

    public static BrowseScreen all(Screen parent){
        return new BrowseScreen(parent, new TabDefinition[]{
                new TabDefinition(ModType.MOD, Component.translatable("modbrowser.gui.browsescreen.tab.mods")),
                new TabDefinition(ModType.RESOURCE_PACK, Component.translatable("modbrowser.gui.browsescreen.tab.resourcepacks")),
                new TabDefinition(ModType.SHADERS, Component.translatable("modbrowser.gui.browsescreen.tab.shaderpacks"))});
    }

    public static BrowseScreen mods(Screen parent){
        return new BrowseScreen(parent, new TabDefinition[]{
                new TabDefinition(ModType.MOD, Component.translatable("modbrowser.gui.browsescreen.tab.mods"))});
    }

    public static BrowseScreen resourcePacks(Screen parent){
        return new BrowseScreen(parent, new TabDefinition[]{
                new TabDefinition(ModType.RESOURCE_PACK, Component.translatable("modbrowser.gui.browsescreen.tab.resourcepacks"))});
    }

    public BrowseScreen(Screen parent, TabDefinition[] tabDefinitions) {
        super(Component.translatable("modbrowser.gui.browsescreen.title"));
        this.parent = parent;
        this.tabDefinitions = tabDefinitions;
    }

    @Nullable
    private static Field findTabBarLayoutField() {
        try {
            Field field = TabNavigationBar.class.getDeclaredField("layout");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void shiftTabBarY(TabNavigationBar tabNavigationBar, int y) {
        if (TAB_BAR_LAYOUT_FIELD == null) {
            return;
        }

        try {
            LinearLayout tabBarLayout = (LinearLayout) TAB_BAR_LAYOUT_FIELD.get(tabNavigationBar);
            tabBarLayout.setY(y);
        } catch (IllegalAccessException ignored) {
        }
    }

    @Override
    protected void init() {
        this.searchBox = new EditBox(this.font, 220, 20, Component.translatable("modbrowser.gui.browsescreen.search"));
        this.searchBox.setHint(Component.translatable("modbrowser.gui.browsescreen.search"));
        this.searchBox.setResponder(text -> this.refreshListsAsync());
        this.addRenderableWidget(this.searchBox);


        TabNavigationBar.Builder builder = TabNavigationBar.builder(this.tabManager, this.width);

        for (TabDefinition tabDefinition : tabDefinitions) {
            tabDefinition.list = new ModSelectionList(this.minecraft, this.width, this.height, 0);
            tabDefinition.list.setEntries(List.of());
            tabDefinition.list.setLoadMoreCallback(() -> this.requestMore(tabDefinition));
            tabDefinition.list.setAddToDownloadCallback(this::addToDownloadList);
            tabDefinition.list.setIsInDownloadListPredicate(this::isInDownloadList);
            builder.addTabs(new DiscoverTab(tabDefinition));
        }
        this.downloadListView = new ModSelectionList(this.minecraft, this.width, this.height, 0);
        this.downloadListView.setEntries(this.downloadList);
        this.downloadListView.setHasMoreEntries(false);
        this.downloadListView.setRemoveFromDownloadCallback(this::removeFromDownloadList);
        this.downloadListView.setIsInDownloadListPredicate(this::isInDownloadList);

        this.tabNavigationBar = builder.addTabs(new DownloadTab()).build();
        this.addRenderableWidget(this.tabNavigationBar);

        LinearLayout footerButtons = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        exitButton = Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).build();
        footerButtons.addChild(exitButton);
        this.layout.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            this.addRenderableWidget(widget);
        });

        this.tabNavigationBar.selectTab(0, false);
        this.refreshListsAsync();
        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        if (this.tabNavigationBar != null) {
            int searchY = 6;
            int searchWidth = Math.min(240, this.width - 20);
            int tabsY = 0;
            if (this.searchBox != null) {
                this.searchBox.setWidth(searchWidth);
                this.searchBox.setPosition((this.width - searchWidth) / 2, searchY);
                tabsY = this.searchBox.getY() + this.searchBox.getHeight() + 6;
            }

            this.tabNavigationBar.setWidth(this.width);
            this.tabNavigationBar.arrangeElements();
            shiftTabBarY(this.tabNavigationBar, tabsY);
            int headerBottom = this.tabNavigationBar.getRectangle().bottom();

            ScreenRectangle tabArea = new ScreenRectangle(0, headerBottom, this.width, this.height - this.layout.getFooterHeight() - headerBottom);

            for (TabDefinition tabDefinition : tabDefinitions) {
                if (tabDefinition.list != null)
                    tabDefinition.list.updateSizeAndPosition(tabArea.width(), tabArea.height(), tabArea.top());
            }

            if (this.downloadListView != null) {
                this.downloadListView.updateSizeAndPosition(tabArea.width(), tabArea.height(), tabArea.top());
            }

            this.tabManager.setTabArea(tabArea);
            this.layout.setHeaderHeight(headerBottom);
            this.layout.arrangeElements();
        }
    }

    private void refreshListsAsync() {
        long requestId = this.searchRequestId.incrementAndGet();
        this.activeSearchGeneration = requestId;
        this.activeSearchQuery = this.searchBox != null ? this.searchBox.getValue() : "";

        for (TabDefinition tabDefinition : tabDefinitions) {
            tabDefinition.nextOffset = 0;
            tabDefinition.moreAvailable = true;
            tabDefinition.loading = false;
            if (tabDefinition.list != null) {
                tabDefinition.list.setLoadingMore(false);
                tabDefinition.list.setHasMoreEntries(true);
                tabDefinition.list.setEntries(List.of());
            }
            this.requestMore(tabDefinition,requestId,this.activeSearchQuery);
        }
    }

    private void requestMore(TabDefinition definition) {
        this.requestMore(definition, this.activeSearchGeneration, this.activeSearchQuery);
    }

    private void requestMore(TabDefinition definition, long generation, @NotNull String query) {
        if (definition.list == null || definition.loading || !definition.moreAvailable) {
            return;
        }

        definition.loading = true;
        definition.list.setLoadingMore(true);

        CompletableFuture<Modrinth.Page> future = Modrinth.searchPageAsync(definition.type, query, definition.nextOffset, PAGE_SIZE);
        future.thenAccept(page -> {
            if (this.minecraft != null) {
                this.minecraft.execute(() -> this.applyPage(definition, generation, page));
            }
        }).exceptionally(throwable -> {
            if (this.minecraft != null) {
                this.minecraft.execute(() -> this.finishLoad(definition, generation));
            }
            return null;
        });
    }

    private void applyPage(TabDefinition definition, long generation, Modrinth.Page page) {
        if (generation != this.searchRequestId.get() || definition.list == null) {
            return;
        }

        this.finishLoad(definition, generation);
        definition.list.appendEntries(page.entries());
        definition.moreAvailable = page.nextOffset() != null;
        definition.list.setHasMoreEntries(definition.moreAvailable);
        if (page.nextOffset() != null) {
            definition.nextOffset = page.nextOffset();
        }
    }

    private void finishLoad(TabDefinition definition, long generation) {
        if (generation != this.searchRequestId.get()) {
            return;
        }

        definition.loading = false;
        if (definition.list != null) {
            definition.list.setLoadingMore(false);
        }
    }

    private void addToDownloadList(@NotNull Mod mod) {
        if (!this.isInDownloadList(mod)) {
            this.downloadList.add(mod);
            status = Component.literal("Added " + mod.name.getString() + " to download list. Resolving dependencies...");
            CompletableFuture<List<Mod>> future = Modrinth.resolveDependenciesAsync(downloadList);
            future.thenAccept(mods -> {
                if (this.minecraft != null) {
                    this.minecraft.execute(() -> {
                        downloadList.clear();
                        downloadList.addAll(mods);
                        status = Component.literal(mods.size() + " mods to download.");
                        this.refreshDownloadListUi();
                    });
                }
            });
        }
    }

    private void removeFromDownloadList(@NotNull Mod mod) {
        if (this.downloadList.remove(mod)) {
            this.status = Component.literal("Removed " + mod.name.getString() + " from download list.");
            this.refreshDownloadListUi();
        }
    }

    private void refreshDownloadListUi() {
        if (this.downloadListView != null) {
            this.downloadListView.setEntries(this.downloadList);
            this.downloadListView.setHasMoreEntries(false);
        }

        if (this.exitButton != null) {
            this.exitButton.setMessage(this.downloadList.isEmpty()
                    ? CommonComponents.GUI_BACK
                    : Component.translatable("modbrowser.gui.browsescreen.buttons.download"));
        }
    }

    private boolean isInDownloadList(@NotNull Mod mod) {
        return downloadList.contains(mod);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.tabNavigationBar != null && this.tabNavigationBar.keyPressed(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            if (!downloadList.isEmpty()) {
                this.minecraft.setScreen(new ConfirmDownloadScreen(this, parent, downloadList));
            } else {
                this.minecraft.setScreen(this.parent);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int searchWidth = Math.min(240, this.width - 20);
        guiGraphics.drawScrollingString(this.font, this.status, 10, (this.width - searchWidth) / 2 - 20, 10, 0xA0A0A0);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics) {
        guiGraphics.blit(TAB_HEADER_BACKGROUND, 0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderMenuBackground(guiGraphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    private class DiscoverTab extends GridLayoutTab {

        DiscoverTab(TabDefinition definition) {
            super(definition.title);
            GridLayout.RowHelper rows = this.layout.rowSpacing(8).createRowHelper(1);
            if (definition.list != null) {
                rows.addChild(definition.list);
            }
        }
    }

    private class DownloadTab extends GridLayoutTab {
        private static final Component TITLE = Component.translatable("modbrowser.gui.browsescreen.tab.downloads");

        DownloadTab() {
            super(TITLE);
            GridLayout.RowHelper rows = this.layout.rowSpacing(8).createRowHelper(1);
            if (BrowseScreen.this.downloadListView != null) {
                rows.addChild(BrowseScreen.this.downloadListView);
            }
        }
    }

}
