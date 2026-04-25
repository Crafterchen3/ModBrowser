package com.deckerpw.modbrowser.gui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.deckerpw.modbrowser.data.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ModSelectionList extends ObjectSelectionList<ModSelectionList.ListEntry> {
    private static final int ROW_HEIGHT = 40;
    private static final int ICON_SIZE = 20;
    private static final int LOAD_MORE_THRESHOLD = ROW_HEIGHT * 2;
    private final List<Mod> allEntries = new ArrayList<>();
    private String currentFilter = "";
    private String expandedModId;
    private boolean loadingMore;
    private boolean hasMoreEntries = true;
    private Runnable loadMoreCallback;

    public ModSelectionList(Minecraft minecraft, int width, int height, int y) {
        super(minecraft, width, height, y, ROW_HEIGHT);
        this.setRenderHeader(false, 0);
    }

    public void setEntries(@NotNull List<Mod> entries) {
        this.allEntries.clear();
        this.allEntries.addAll(entries);
        this.expandedModId = null;
        this.applyFilter(this.currentFilter);
    }

    public void appendEntries(@NotNull List<Mod> entries) {
        this.allEntries.addAll(entries);
        this.applyFilter(this.currentFilter);
    }

    public void setLoadMoreCallback(@NotNull Runnable loadMoreCallback) {
        this.loadMoreCallback = loadMoreCallback;
    }

    public void setLoadingMore(boolean loadingMore) {
        this.loadingMore = loadingMore;
    }

    public void setHasMoreEntries(boolean hasMoreEntries) {
        this.hasMoreEntries = hasMoreEntries;
    }

    public void applyFilter(String searchQuery) {
        this.currentFilter = searchQuery == null ? "" : searchQuery;
        String filter = this.currentFilter.trim().toLowerCase(Locale.ROOT);
        this.clearEntries();
        ModEntry selectedEntry = null;

        for (Mod info : this.allEntries) {
            if (filter.isEmpty() || matchesFilter(info, filter)) {
                ModEntry modEntry = new ModEntry(info);
                this.addEntry(modEntry);
                if (info.id().equals(this.expandedModId)) {
                    selectedEntry = modEntry;
                }
                if (info.id().equals(this.expandedModId)) {
                    this.addEntry(new DescriptionEntry(info));
                }
            }
        }

        this.setSelected(selectedEntry);
    }

    @Override
    @NotNull
    public int getRowWidth() {
        return Math.min(360, this.width - 16);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        this.maybeRequestMoreEntries();
    }

    private void maybeRequestMoreEntries() {
        if (this.loadMoreCallback == null || this.loadingMore || !this.hasMoreEntries) {
            return;
        }

        double remainingScroll = this.getMaxScroll() - this.getScrollAmount();
        if (remainingScroll <= LOAD_MORE_THRESHOLD) {
            this.loadMoreCallback.run();
        }
    }

    private static boolean matchesFilter(Mod info, String filter) {
        return info.id().toLowerCase(Locale.ROOT).contains(filter)
            || info.name().getString().toLowerCase(Locale.ROOT).contains(filter)
            || info.summary().getString().toLowerCase(Locale.ROOT).contains(filter);
    }

    static abstract class ListEntry extends ObjectSelectionList.Entry<ListEntry> {
    }

    private class ModEntry extends ListEntry {
        private final Mod info;

        private ModEntry(Mod info) {
            this.info = info;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.info.name());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                ModSelectionList.this.setSelected(this);
                if (this.info.id().equals(ModSelectionList.this.expandedModId)) {
                    ModSelectionList.this.expandedModId = null;
                } else {
                    ModSelectionList.this.expandedModId = this.info.id();
                }
                ModSelectionList.this.applyFilter(ModSelectionList.this.currentFilter);
                return true;
            }

            return false;
        }

        @Override
        public void render(
            GuiGraphics guiGraphics,
            int index,
            int top,
            int left,
            int width,
            int height,
            int mouseX,
            int mouseY,
            boolean hovered,
            float partialTick
        ) {
            int iconLeft = left + 6;
            int iconTop = top + (height - ICON_SIZE) / 2;
            int textLeft = iconLeft + ICON_SIZE + 8;
            int titleColor = ModSelectionList.this.getSelected() == this ? 0xFFE5A0 : 0xFFFFFF;

            // Simple placeholder icon: first letter over a tinted square.
            guiGraphics.fill(iconLeft, iconTop, iconLeft + ICON_SIZE, iconTop + ICON_SIZE, 0xFF3A3A3A);
            String firstLetter = this.info.name().getString().isEmpty()
                ? "?"
                : this.info.name().getString().substring(0, 1).toUpperCase(Locale.ROOT);
            int letterWidth = ModSelectionList.this.minecraft.font.width(firstLetter);
            guiGraphics.drawString(ModSelectionList.this.minecraft.font, firstLetter, iconLeft + (ICON_SIZE - letterWidth) / 2, iconTop + 6, 0xFFFFFFFF, false);

            guiGraphics.drawString(ModSelectionList.this.minecraft.font, this.info.name(), textLeft, top + 5, titleColor, false);
            guiGraphics.drawString(ModSelectionList.this.minecraft.font, Component.literal("ID: " + this.info.id()), textLeft, top + 17, 0xA0A0A0, false);
        }
    }

    private class DescriptionEntry extends ListEntry {
        private final Mod info;

        private DescriptionEntry(Mod info) {
            this.info = info;
        }

        @Override
        public Component getNarration() {
            return this.info.summary();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public void render(
            GuiGraphics guiGraphics,
            int index,
            int top,
            int left,
            int width,
            int height,
            int mouseX,
            int mouseY,
            boolean hovered,
            float partialTick
        ) {
            int textLeft = left + 6 + ICON_SIZE + 8;
            guiGraphics.drawWordWrap(ModSelectionList.this.minecraft.font, this.info.summary(), textLeft, top + 3, Math.max(40, width - (ICON_SIZE + 20)), 0xBFBFBF);
        }
    }
}

