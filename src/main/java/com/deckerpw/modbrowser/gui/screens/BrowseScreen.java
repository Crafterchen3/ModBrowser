package com.deckerpw.modbrowser.gui.screens;

import com.deckerpw.modbrowser.api.Modrinth;
import com.deckerpw.modbrowser.data.Mod;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class BrowseScreen extends Screen {
    private static final int PAGE_SIZE = 1;
    private static final ResourceLocation TAB_HEADER_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/tab_header_background.png");
    private static final Field TAB_BAR_LAYOUT_FIELD = findTabBarLayoutField();
    private final AtomicLong searchRequestId = new AtomicLong();
    private final Screen parent;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private final List<Mod> downloadList = new ArrayList<>();
    private long activeSearchGeneration;
    @NotNull
    private String activeSearchQuery = "";
    private int nextModOffset;
    private int nextResourcePackOffset;
    private boolean moreModsAvailable = true;
    private boolean moreResourcePacksAvailable = true;
    private boolean modsLoading;
    private boolean resourcePacksLoading;
    @Nullable
    private EditBox searchBox;
    @Nullable
    private ModSelectionList modsList;
    @Nullable
    private ModSelectionList downloadListView;
    @Nullable
    private ModSelectionList resourcePackList;
    @Nullable
    private TabNavigationBar tabNavigationBar;
    private Component status = Component.empty();
    private Button exitButton;

    public BrowseScreen(Screen parent) {
        super(Component.translatable("modbrowser.gui.browsescreen.title"));
        this.parent = parent;
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

        this.modsList = new ModSelectionList(this.minecraft, this.width, this.height, 0);
        this.modsList.setEntries(List.of());
        this.modsList.setLoadMoreCallback(this::requestMoreMods);
        this.modsList.setAddToDownloadCallback(this::addToDownloadList);
        this.modsList.setIsInDownloadListPredicate(this::isInDownloadList);
        this.downloadListView = new ModSelectionList(this.minecraft, this.width, this.height, 0);
        this.downloadListView.setEntries(this.downloadList);
        this.downloadListView.setHasMoreEntries(false);
        this.resourcePackList = new ModSelectionList(this.minecraft, this.width, this.height, 0);
        this.resourcePackList.setEntries(List.of());
        this.resourcePackList.setLoadMoreCallback(this::requestMoreResourcePacks);
        this.resourcePackList.setAddToDownloadCallback(this::addToDownloadList);
        this.resourcePackList.setIsInDownloadListPredicate(this::isInDownloadList);

        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
                .addTabs(new ModsDiscoverTab(), new ResourcePackDiscoverTab(), new DownloadTab())
                .build();
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
            if (this.modsList != null) {
                this.modsList.updateSizeAndPosition(tabArea.width(), tabArea.height(), tabArea.top());
            }

            if (this.downloadListView != null) {
                this.downloadListView.updateSizeAndPosition(tabArea.width(), tabArea.height(), tabArea.top());
            }

            if (this.resourcePackList != null) {
                this.resourcePackList.updateSizeAndPosition(tabArea.width(), tabArea.height(), tabArea.top());
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
        this.nextModOffset = 0;
        this.nextResourcePackOffset = 0;
        this.moreModsAvailable = true;
        this.moreResourcePacksAvailable = true;
        this.modsLoading = false;
        this.resourcePacksLoading = false;

        if (this.modsList != null) {
            this.modsList.setLoadingMore(false);
            this.modsList.setHasMoreEntries(true);
            this.modsList.setEntries(List.of());
        }

        if (this.resourcePackList != null) {
            this.resourcePackList.setLoadingMore(false);
            this.resourcePackList.setHasMoreEntries(true);
            this.resourcePackList.setEntries(List.of());
        }

        this.requestMoreMods(requestId, this.activeSearchQuery);
        this.requestMoreResourcePacks(requestId, this.activeSearchQuery);
    }

    private void requestMoreMods() {
        this.requestMoreMods(this.activeSearchGeneration, this.activeSearchQuery);
    }

    private void requestMoreResourcePacks() {
        this.requestMoreResourcePacks(this.activeSearchGeneration, this.activeSearchQuery);
    }

    private void requestMoreMods(long generation, @NotNull String query) {
        if (this.modsList == null || this.modsLoading || !this.moreModsAvailable) {
            return;
        }

        this.modsLoading = true;
        this.modsList.setLoadingMore(true);

        CompletableFuture<Modrinth.Page> future = Modrinth.searchModsPageAsync(query, this.nextModOffset, PAGE_SIZE);
        future.thenAccept(page -> {
            if (this.minecraft != null) {
                this.minecraft.execute(() -> this.applyModsPage(generation, page));
            }
        }).exceptionally(throwable -> {
            if (this.minecraft != null) {
                this.minecraft.execute(() -> this.finishModsLoad(generation));
            }
            return null;
        });
    }

    private void requestMoreResourcePacks(long generation, @NotNull String query) {
        if (this.resourcePackList == null || this.resourcePacksLoading || !this.moreResourcePacksAvailable) {
            return;
        }

        this.resourcePacksLoading = true;
        this.resourcePackList.setLoadingMore(true);

        CompletableFuture<Modrinth.Page> future = Modrinth.searchResourcePacksPageAsync(query, this.nextResourcePackOffset, PAGE_SIZE);
        future.thenAccept(page -> {
            if (this.minecraft != null) {
                this.minecraft.execute(() -> this.applyResourcePackPage(generation, page));
            }
        }).exceptionally(throwable -> {
            if (this.minecraft != null) {
                this.minecraft.execute(() -> this.finishResourcePackLoad(generation));
            }
            return null;
        });
    }

    private void applyModsPage(long generation, Modrinth.Page page) {
        if (generation != this.searchRequestId.get() || this.modsList == null) {
            return;
        }

        this.finishModsLoad(generation);
        this.modsList.appendEntries(page.entries());
        this.moreModsAvailable = page.nextOffset() != null;
        this.modsList.setHasMoreEntries(this.moreModsAvailable);
        if (page.nextOffset() != null) {
            this.nextModOffset = page.nextOffset();
        }
    }

    private void applyResourcePackPage(long generation, Modrinth.Page page) {
        if (generation != this.searchRequestId.get() || this.resourcePackList == null) {
            return;
        }

        this.finishResourcePackLoad(generation);
        this.resourcePackList.appendEntries(page.entries());
        this.moreResourcePacksAvailable = page.nextOffset() != null;
        this.resourcePackList.setHasMoreEntries(this.moreResourcePacksAvailable);
        if (page.nextOffset() != null) {
            this.nextResourcePackOffset = page.nextOffset();
        }
    }

    private void finishModsLoad(long generation) {
        if (generation != this.searchRequestId.get()) {
            return;
        }

        this.modsLoading = false;
        if (this.modsList != null) {
            this.modsList.setLoadingMore(false);
        }
    }

    private void finishResourcePackLoad(long generation) {
        if (generation != this.searchRequestId.get()) {
            return;
        }

        this.resourcePacksLoading = false;
        if (this.resourcePackList != null) {
            this.resourcePackList.setLoadingMore(false);
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
                        if (this.downloadListView != null) {
                            this.downloadListView.setEntries(this.downloadList);
                            this.downloadListView.setHasMoreEntries(false);
                        }
                        if(!this.downloadList.isEmpty()){
                            exitButton.setMessage(Component.translatable("modbrowser.gui.browsescreen.buttons.download"));

                        }
                    });
                }
            });
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
        guiGraphics.drawScrollingString(this.font, this.status, 10, (this.width - searchWidth) / 2 - 20,10, 0xA0A0A0);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics) {
        guiGraphics.blit(TAB_HEADER_BACKGROUND, 0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderMenuBackground(guiGraphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    private class ModsDiscoverTab extends GridLayoutTab {
        private static final Component TITLE = Component.translatable("modbrowser.gui.browsescreen.tab.mods");

        ModsDiscoverTab() {
            super(TITLE);
            GridLayout.RowHelper rows = this.layout.rowSpacing(8).createRowHelper(1);
            if (BrowseScreen.this.modsList != null) {
                rows.addChild(BrowseScreen.this.modsList);
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

    private class ResourcePackDiscoverTab extends GridLayoutTab {
        private static final Component TITLE = Component.translatable("modbrowser.gui.browsescreen.tab.resourcepacks");

        ResourcePackDiscoverTab() {
            super(TITLE);
            GridLayout.RowHelper rows = this.layout.rowSpacing(8).createRowHelper(1);
            if (BrowseScreen.this.resourcePackList != null) {
                rows.addChild(BrowseScreen.this.resourcePackList);
            }
        }
    }

}
