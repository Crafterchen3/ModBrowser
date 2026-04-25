package com.deckerpw.modbrowser.gui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.deckerpw.modbrowser.data.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ModSelectionList extends ObjectSelectionList<ModSelectionList.ListEntry> {
    private static final int ROW_HEIGHT = 40;
    private static final int ICON_SIZE = 20;
    private static final int ACTION_BUTTON_WIDTH = 56;
    private static final int ACTION_BUTTON_HEIGHT = 20;
    private static final int ACTION_BUTTON_RIGHT_PADDING = 6;
    private static final int LOAD_MORE_THRESHOLD = ROW_HEIGHT * 2;
    private final List<Mod> allEntries = new ArrayList<>();
    private String currentFilter = "";
    private String expandedModId;
    private boolean loadingMore;
    private boolean hasMoreEntries = true;
    private Runnable loadMoreCallback;
    private Consumer<Mod> addToDownloadCallback;
    private Consumer<Mod> removeFromDownloadCallback;
    private Predicate<Mod> isInDownloadList = mod -> false;

    public ModSelectionList(Minecraft minecraft, int width, int height, int y) {
        super(minecraft, width, height, y, ROW_HEIGHT);
        this.setRenderHeader(false, 0);
    }

    public void setEntries(@NotNull List<Mod> entries) {
        this.setScrollAmount(0);
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

    public void setAddToDownloadCallback(@NotNull Consumer<Mod> addToDownloadCallback) {
        this.addToDownloadCallback = addToDownloadCallback;
    }

    public void setRemoveFromDownloadCallback(@NotNull Consumer<Mod> removeFromDownloadCallback) {
        this.removeFromDownloadCallback = removeFromDownloadCallback;
    }

    public void setIsInDownloadListPredicate(@NotNull Predicate<Mod> isInDownloadList) {
        this.isInDownloadList = isInDownloadList;
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
                if (info.id.equals(this.expandedModId)) {
                    selectedEntry = modEntry;
                    this.addEntry(new DescriptionEntry(info));
                }
            }
        }

        this.setSelected(selectedEntry);
        addEntry(new FooterEntry());
    }

    @Override
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
        return info.id.toLowerCase(Locale.ROOT).contains(filter)
            || info.name.getString().toLowerCase(Locale.ROOT).contains(filter)
            || info.summary.getString().toLowerCase(Locale.ROOT).contains(filter);
    }

    static abstract class ListEntry extends ObjectSelectionList.Entry<ListEntry> {
    }

    private class ModEntry extends ListEntry {
        private final Mod info;
        private int lastLeft;
        private int lastTop;
        private int lastWidth;
        private int lastHeight;

        private ModEntry(Mod info) {
            this.info = info;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("narrator.select", this.info.name);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && this.isOverActionButton(mouseX, mouseY, this.lastLeft, this.lastTop, this.lastWidth, this.lastHeight)) {
                boolean queued = ModSelectionList.this.isInDownloadList.test(this.info);
                if (queued && ModSelectionList.this.removeFromDownloadCallback != null) {
                    ModSelectionList.this.removeFromDownloadCallback.accept(this.info);
                } else if (!queued && ModSelectionList.this.addToDownloadCallback != null) {
                    ModSelectionList.this.addToDownloadCallback.accept(this.info);
                }
                return true;
            }

            if (button == 0) {
                ModSelectionList.this.setSelected(this);
                if (this.info.id.equals(ModSelectionList.this.expandedModId)) {
                    ModSelectionList.this.expandedModId = null;
                } else {
                    ModSelectionList.this.expandedModId = this.info.id;
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
            this.lastLeft = left;
            this.lastTop = top;
            this.lastWidth = width;
            this.lastHeight = height;
            int iconLeft = left + 6;
            int iconTop = top + (height - ICON_SIZE) / 2;
            int textLeft = iconLeft + ICON_SIZE + 8;
            int titleColor = ModSelectionList.this.getSelected() == this ? 0xFFE5A0 : 0xFFFFFF;
            int buttonLeft = this.actionButtonLeft(left, width);
            int buttonTop = this.actionButtonTop(top, height);
            boolean showButton = ModSelectionList.this.addToDownloadCallback != null || ModSelectionList.this.removeFromDownloadCallback != null;

            // Simple placeholder icon: first letter over a tinted square.
            guiGraphics.fill(iconLeft, iconTop, iconLeft + ICON_SIZE, iconTop + ICON_SIZE, 0xFF3A3A3A);
            String firstLetter = this.info.name.getString().isEmpty()
                ? "?"
                : this.info.name.getString().substring(0, 1).toUpperCase(Locale.ROOT);
            int letterWidth = ModSelectionList.this.minecraft.font.width(firstLetter);
            guiGraphics.drawString(ModSelectionList.this.minecraft.font, firstLetter, iconLeft + (ICON_SIZE - letterWidth) / 2, iconTop + 6, 0xFFFFFFFF, false);

            guiGraphics.drawString(ModSelectionList.this.minecraft.font, this.info.name, textLeft, top + 5, titleColor, false);
            if (this.info.version != null && !showButton)
                guiGraphics.drawString(ModSelectionList.this.minecraft.font, Component.literal(this.info.version.getFiles().getFirst().getFilename()), textLeft, top + 17, 0xA0A0A0, false);
            else
                guiGraphics.drawString(ModSelectionList.this.minecraft.font, Component.literal("by " + this.info.author), textLeft, top + 17, 0xA0A0A0, false);

            if (showButton) {
                boolean queued = ModSelectionList.this.isInDownloadList.test(this.info);
                boolean hoveredButton = this.isMouseOverButton(mouseX, mouseY, buttonLeft, buttonTop);
                boolean canRemove = queued && ModSelectionList.this.removeFromDownloadCallback != null;
                int fillColor = canRemove
                    ? (hoveredButton ? 0xFFA33A3A : 0xFF8C2F2F)
                    : queued ? 0xFF2E7D32 : hoveredButton ? 0xFF5A5A5A : 0xFF3F3F3F;
                guiGraphics.fill(buttonLeft, buttonTop, buttonLeft + ACTION_BUTTON_WIDTH, buttonTop + ACTION_BUTTON_HEIGHT, fillColor);
                guiGraphics.drawCenteredString(
                    ModSelectionList.this.minecraft.font,
                    canRemove ? Component.literal("Remove") : queued ? Component.literal("Added") : Component.literal("Add"),
                    buttonLeft + ACTION_BUTTON_WIDTH / 2,
                    buttonTop + 6,
                    0xFFFFFFFF
                );
            }
        }

        private int actionButtonLeft(int left, int width) {
            return left + width - ACTION_BUTTON_WIDTH - ACTION_BUTTON_RIGHT_PADDING;
        }

        private int actionButtonTop(int top, int height) {
            return top + (height - ACTION_BUTTON_HEIGHT) / 2;
        }

        private boolean isOverActionButton(double mouseX, double mouseY, int left, int top, int width, int height) {
            int buttonLeft = this.actionButtonLeft(left, width);
            int buttonTop = this.actionButtonTop(top, height);
            return (ModSelectionList.this.addToDownloadCallback != null || ModSelectionList.this.removeFromDownloadCallback != null)
                && mouseX >= buttonLeft
                && mouseX < buttonLeft + ACTION_BUTTON_WIDTH
                && mouseY >= buttonTop
                && mouseY < buttonTop + ACTION_BUTTON_HEIGHT;
        }

        private boolean isMouseOverButton(double mouseX, double mouseY, int buttonLeft, int buttonTop) {
            return mouseX >= buttonLeft
                && mouseX < buttonLeft + ACTION_BUTTON_WIDTH
                && mouseY >= buttonTop
                && mouseY < buttonTop + ACTION_BUTTON_HEIGHT;
        }
    }

    private class DescriptionEntry extends ListEntry {
        private final Mod info;

        private DescriptionEntry(Mod info) {
            this.info = info;
        }

        @Override
        public @NotNull Component getNarration() {
            return this.info.summary;
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
            guiGraphics.drawWordWrap(ModSelectionList.this.minecraft.font, this.info.summary, textLeft, top + 3, Math.max(40, width - (ICON_SIZE + 20)), 0xBFBFBF);
        }
    }

    private class FooterEntry extends ListEntry{

        @Override
        public @NotNull Component getNarration() {
            return Component.literal("Footer");
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            guiGraphics.drawCenteredString(ModSelectionList.this.minecraft.font, Component.literal(loadingMore ? "Loading..." : "End of list"), left + width / 2, top + (height - 8) / 2, 0xBFBFBF);
        }
    }
}

