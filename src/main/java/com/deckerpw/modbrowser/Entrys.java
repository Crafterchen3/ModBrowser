package com.deckerpw.modbrowser;

import com.deckerpw.modbrowser.gui.BrowseScreen;
import com.deckerpw.modbrowser.gui.component.ObjectSelectionList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Entrys {


    @OnlyIn(Dist.CLIENT)
    public static class BrowseListEntry extends ObjectSelectionList.Entry<Entrys.BrowseListEntry> implements AutoCloseable {
        private static final int ICON_WIDTH = 32;
        private static final int ICON_HEIGHT = 32;
        private static final int ICON_OVERLAY_X_JOIN = 0;
        private static final int ICON_OVERLAY_X_JOIN_WITH_NOTIFY = 32;
        private static final int ICON_OVERLAY_X_WARNING = 64;
        private static final int ICON_OVERLAY_X_ERROR = 96;
        private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
        private static final int ICON_OVERLAY_Y_SELECTED = 32;
        public final ObjectSelectionList<BrowseListEntry> parent;
        private final Minecraft minecraft;
        public final Mod mod;
        public final @Nullable BrowseScreen screen;
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel summaryDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;
        private long lastClickTime;
        private ResourceLocation logo;

        protected float overlayX;
        private boolean last = true;


        public BrowseListEntry(Minecraft p_100084_, ObjectSelectionList<BrowseListEntry> p_100085_, @Nullable BrowseScreen p_100086_, Mod p_100087_) {
            this(p_100084_, p_100085_, p_100086_, p_100087_, true);
        }

        public BrowseListEntry(Minecraft p_100084_, ObjectSelectionList<BrowseListEntry> p_100085_, @Nullable BrowseScreen p_100086_, Mod p_100087_, boolean dont) {
            super(p_100087_);
            this.screen = p_100086_;
            this.mod = p_100087_;
            this.minecraft = p_100084_;
            this.parent = p_100085_;
            this.nameDisplayCache = cacheName(p_100084_, p_100087_.getTitle());
            this.summaryDisplayCache = cacheSummary(minecraft,mod);
            this.descriptionDisplayCache = cacheDescription(minecraft,mod.getDescription());
            logo = BrowseScreen.LOADING_ICON;
            
            this.last = dont;
            overlayX = 0F;
        }
        private static FormattedCharSequence cacheName(Minecraft p_100105_, Component p_100106_) {
            int i = p_100105_.font.width(p_100106_);
            if (i > 157+50) {
                FormattedText formattedtext = FormattedText.composite(p_100105_.font.substrByWidth(p_100106_, 157+50 - p_100105_.font.width("...")), FormattedText.of("..."));
                return Language.getInstance().getVisualOrder(formattedtext);
            } else {
                return p_100106_.getVisualOrderText();
            }
        }

        private static MultiLineLabel cacheDescription(Minecraft p_100110_, Component p_100111_) {
            return MultiLineLabel.create(p_100110_.font, p_100111_, 157+50, 3);
        }



        private static MultiLineLabel cacheSummary(Minecraft mc, Mod mod) {
            List<Component> components = new ArrayList<>();
            Component comp = new TranslatableComponent("entry.author.instr",mod.authors);

            int i = mc.font.width(comp);
            if (i > 157+50) {
                components.add(Component.nullToEmpty(FormattedText.composite(mc.font.substrByWidth(comp, 157+50 - mc.font.width("...")), FormattedText.of("...")).getString()));
            }else {
                components.add(comp);
            }

            comp = Component.nullToEmpty(mod.category);
            i = mc.font.width(comp);
            if (i > 157+50) {
                components.add(Component.nullToEmpty(FormattedText.composite(mc.font.substrByWidth(comp, 157 + 50 - mc.font.width("...")), FormattedText.of("...")).getString()));
            }else {
                components.add(comp);
            }
            return MultiLineLabel.create(mc.font,components);
        }



        public Component getNarration() {
            return new TranslatableComponent("narrator.select", mod.getTitle());
        }

        private void generateLogoRessource() throws IOException {

            TextureManager manager = minecraft.getTextureManager();
            if (mod.logo != null){
                logo = mod.logo;
            }else {
                logo = manager.register(mod.id+"_logo",mod.getLogoURL());
            }
        }


        public void render(PoseStack p_101721_, int p_101722_, int p_101723_, int p_101724_, int p_101725_, int p_101726_, int p_101727_, int p_101728_, boolean p_101729_, float p_101730_) {
            if (mod.modType == ModBrowser.ModType.FILES)
                this.overlayX = 32F;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (mod.id != ModBrowser.GHOST_ID && logo == BrowseScreen.LOADING_ICON) {
                //TODO add setting
                try {
                    generateLogoRessource();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            RenderSystem.setShaderTexture(0, logo);

            RenderSystem.enableBlend();
            GuiComponent.blit(p_101721_, p_101724_, p_101723_, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if (this.minecraft.options.touchscreen || p_101729_) {
                RenderSystem.setShaderTexture(0, new ResourceLocation(ModBrowser.MOD_ID,"textures/gui/widgets.png"));
                GuiComponent.fill(p_101721_, p_101724_, p_101723_, p_101724_ + 32, p_101723_ + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int i = p_101727_ - p_101724_;
                boolean flag = i < 32;
                int j = flag ? 32 : 0;
                GuiComponent.blit(p_101721_, p_101724_, p_101723_, overlayX, (float) j, 32, 32, 256*2, 256*2);

            }
            this.minecraft.font.drawShadow(p_101721_, nameDisplayCache, (float)(p_101724_ + 32 + 2), (float)(p_101723_ + 1), 16777215);
            summaryDisplayCache.renderLeftAligned(p_101721_, p_101724_ + 32 + 2, p_101723_ + 12, 10, 8421504);

            if (parent.getSelected() != null && parent.getSelected().equals(this))
                descriptionDisplayCache.renderLeftAligned(p_101721_, p_101724_ + 2, p_101723_ + 32 + 2 , 10, 16777215);
            if (last){
                screen.loadMore();
                last = false;
            }
        }

        public void renderPreview(PoseStack p_101721_, int p_101722_, int p_101723_, int p_101724_, int p_101725_, int p_101726_, int p_101727_, int p_101728_, boolean p_101729_, float p_101730_) {

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, logo);

            RenderSystem.enableBlend();
            GuiComponent.blit(p_101721_, p_101724_, p_101723_, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if (this.minecraft.options.touchscreen || p_101729_) {
                RenderSystem.setShaderTexture(0, new ResourceLocation(ModBrowser.MOD_ID,"textures/gui/widgets.png"));
                GuiComponent.fill(p_101721_, p_101724_, p_101723_, p_101724_ + 32, p_101723_ + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int i = p_101727_ - p_101724_;
                boolean flag = i < 32;
                int j = flag ? 32 : 0;
                GuiComponent.blit(p_101721_, p_101724_, p_101723_, overlayX, (float) j, 32, 32, 256*2, 256*2);

            }
            this.minecraft.font.drawShadow(p_101721_, nameDisplayCache, (float)(p_101724_ + 32 + 2), (float)(p_101723_ + 1), 16777215);
            summaryDisplayCache.renderLeftAligned(p_101721_, p_101724_ + 32 + 2, p_101723_ + 12, 10, 8421504);
            descriptionDisplayCache.renderLeftAligned(p_101721_, p_101724_ + 2, p_101723_ + 32 + 2 , 10, 16777215);
        }

        public boolean mouseClicked(double p_101706_, double p_101707_, int p_101708_) {
            if (p_101706_ - (double) parent.getRowLeft() <= 32.0D) {
                screen.selectMod(mod.id);
                return true;
            }
            parent.setSelected(this);
            return false;
        }

        @Override
        public void close() throws Exception {

        }
    }

}
