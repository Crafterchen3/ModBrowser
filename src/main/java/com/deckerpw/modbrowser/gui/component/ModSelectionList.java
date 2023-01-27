package com.deckerpw.modbrowser.gui.component;

import com.deckerpw.modbrowser.Curseforge;
import com.deckerpw.modbrowser.Mod;
import com.deckerpw.modbrowser.ModBrowser;
import com.deckerpw.modbrowser.gui.BrowseScreen;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModSelectionList extends ObjectSelectionList<ModSelectionList.ModListEntry> {
    static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
    private final BrowseScreen screen;
    protected int itemHeight  = 36;
    private  int selected = Integer.MAX_VALUE;

    public ModSelectionList(BrowseScreen p_101658_, Minecraft p_101659_, int p_101660_, int p_101661_, int p_101662_, int p_101663_, int p_101664_) {
        super(p_101659_, p_101660_, p_101661_, p_101662_, p_101663_, p_101664_);
        this.screen = p_101658_;
    }

    @Override
    public void reset() {
        super.reset();
        this.selected = Integer.MAX_VALUE;
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    protected boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    public void setSelected(@Nullable ModListEntry p_101675_) {
        super.setSelected(p_101675_);
        selected = children().indexOf(p_101675_);
    }

    protected void moveSelection(AbstractSelectionList.SelectionDirection p_101673_) {
        this.moveSelection(p_101673_, (p_101681_) -> {
            return !p_101681_.mod.getCompatibility().isCompatible();
        });
    }
    @Override
    @Nullable
    public ModListEntry getEntryAtPosition(double x, double y) {
        int i = this.getRowWidth() / 2;
        int j = this.x0 + this.width / 2;
        int k = j - i;
        int l = j + i;
        int i1 = Mth.floor(y - (double)this.y0)/*48*/ - this.headerHeight/*0*/ + (int)this.getScrollAmount() - 4;
        int j1 = i1 / this.itemHeight;
        j1 -= j1 > selected ? 1: 0;
        return x < (double)this.getScrollbarPosition() && x >= (double)k && x <= (double)l && j1 >= 0 && i1 >= 0 && j1 < this.getItemCount() ? this.children().get(j1) : null;
    }

    @Override
    protected int getRowTop(int p_93512_) {
        return this.y0 + 4 - (int)this.getScrollAmount() + p_93512_ * 36 + this.headerHeight + (p_93512_ > selected  ? 36 : 0 );
    }
    @Override
    protected void renderList(PoseStack p_93452_, int p_93453_, int p_93454_, int p_93455_, int p_93456_, float p_93457_) {
        int i = this.getItemCount();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        for(int j = 0; j < i; ++j) {
            itemHeight = selected == j ? 36*2 : 36;
            int k = this.getRowTop(j);
            int l = this.getRowBottom(j);
            if (l >= this.y0 && k <= this.y1) {
                int i1 = p_93454_ + j * 36 + this.headerHeight;
                int j1 = this.itemHeight - 4;
                ModListEntry e = this.getEntry(j);
                int k1 = this.getRowWidth();
                if (this.isSelectedItem(j)) {
                    int l1 = this.x0 + this.width / 2 - k1 / 2;
                    int i2 = this.x0 + this.width / 2 + k1 / 2;
                    RenderSystem.disableTexture();
                    RenderSystem.setShader(GameRenderer::getPositionShader);
                    float f = this.isFocused() ? 1.0F : 0.5F;
                    RenderSystem.setShaderColor(f, f, f, 1.0F);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferbuilder.vertex((double)l1, (double)(i1 + j1 + 2), 0.0D).endVertex();
                    bufferbuilder.vertex((double)i2, (double)(i1 + j1 + 2), 0.0D).endVertex();
                    bufferbuilder.vertex((double)i2, (double)(i1 - 2), 0.0D).endVertex();
                    bufferbuilder.vertex((double)l1, (double)(i1 - 2), 0.0D).endVertex();
                    tesselator.end();
                    RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferbuilder.vertex((double)(l1 + 1), (double)(i1 + j1 + 1), 0.0D).endVertex();
                    bufferbuilder.vertex((double)(i2 - 1), (double)(i1 + j1 + 1), 0.0D).endVertex();
                    bufferbuilder.vertex((double)(i2 - 1), (double)(i1 - 1), 0.0D).endVertex();
                    bufferbuilder.vertex((double)(l1 + 1), (double)(i1 - 1), 0.0D).endVertex();
                    tesselator.end();
                    RenderSystem.enableTexture();
                }

                int j2 = this.getRowLeft();
                e.render(p_93452_, j, k, j2, k1, j1, p_93455_, p_93456_, Objects.equals(this.getHovered(), e), p_93457_);
            }
        }

    }

    public Optional<ModListEntry> getSelectedOpt() {
        return Optional.ofNullable(this.getSelected());
    }

    public Screen getScreen() {
        return this.screen;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class ModListEntry extends ObjectSelectionList.Entry<ModListEntry> {
        private static final int ICON_WIDTH = 32;
        private static final int ICON_HEIGHT = 32;
        private static final int ICON_OVERLAY_X_JOIN = 0;
        private static final int ICON_OVERLAY_X_JOIN_WITH_NOTIFY = 32;
        private static final int ICON_OVERLAY_X_WARNING = 64;
        private static final int ICON_OVERLAY_X_ERROR = 96;
        private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
        private static final int ICON_OVERLAY_Y_SELECTED = 32;
        private final ModSelectionList parent;
        private final Minecraft minecraft;
        public final Mod mod;
        private final Screen screen;
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel summaryDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;
        private long lastClickTime;
        private ResourceLocation logo;
        private final Curseforge cf;
        private boolean last = true;

        public ModListEntry(Minecraft p_100084_, ModSelectionList p_100085_, Screen p_100086_, Mod p_100087_, Curseforge cf) {
            this.screen = p_100086_;
            this.mod = p_100087_;
            this.minecraft = p_100084_;
            this.parent = p_100085_;
            this.cf = cf;
            this.nameDisplayCache = cacheName(p_100084_, p_100087_.getTitle());
            this.summaryDisplayCache = cacheSummary(minecraft,mod);
            this.descriptionDisplayCache = cacheDescription(minecraft,mod.getDescription());
            logo = BrowseScreen.DEFAULT_ICON;
            if (mod.id != ModBrowser.GHOST_ID) {
                //TODO add setting
                try {
                    generateLogoRessource();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

        }

        public ModListEntry(Minecraft p_100084_, ModSelectionList p_100085_, Screen p_100086_, Mod p_100087_, Curseforge cf,boolean dont) {
            this.screen = p_100086_;
            this.mod = p_100087_;
            this.minecraft = p_100084_;
            this.parent = p_100085_;
            this.cf = cf;
            this.nameDisplayCache = cacheName(p_100084_, p_100087_.getTitle());
            this.summaryDisplayCache = cacheSummary(minecraft,mod);
            this.descriptionDisplayCache = cacheDescription(minecraft,mod.getDescription());
            logo = BrowseScreen.LOADING_ICON;

            this.last = dont;
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
            Component comp = Component.nullToEmpty("by:"+mod.authors);

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
            HttpURLConnection con = (HttpURLConnection) new URL(mod.logoURL).openConnection();
            TextureManager manager = minecraft.getTextureManager();
            logo = manager.register(mod.id+"_logo",new DynamicTexture(NativeImage.read(con.getInputStream())));
        }

        public void render(PoseStack p_101721_, int p_101722_, int p_101723_, int p_101724_, int p_101725_, int p_101726_, int p_101727_, int p_101728_, boolean p_101729_, float p_101730_) {

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, logo);

            RenderSystem.enableBlend();
            GuiComponent.blit(p_101721_, p_101724_, p_101723_, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if (this.minecraft.options.touchscreen || p_101729_) {
                RenderSystem.setShaderTexture(0, ModSelectionList.ICON_OVERLAY_LOCATION);
                GuiComponent.fill(p_101721_, p_101724_, p_101723_, p_101724_ + 32, p_101723_ + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int i = p_101727_ - p_101724_;
                boolean flag = i < 32;
                int j = flag ? 32 : 0;
                GuiComponent.blit(p_101721_, p_101724_, p_101723_, 0.0F, (float) j, 32, 32, 256, 256);

            }
            this.minecraft.font.drawShadow(p_101721_, nameDisplayCache, (float)(p_101724_ + 32 + 2), (float)(p_101723_ + 1), 16777215);
            summaryDisplayCache.renderLeftAligned(p_101721_, p_101724_ + 32 + 2, p_101723_ + 12, 10, 8421504);

            if (parent.getSelected() != null && parent.getSelected().equals(this))
                descriptionDisplayCache.renderLeftAligned(p_101721_, p_101724_ + 2, p_101723_ + 32 + 2 , 10, 16777215);
            if (last){
                parent.screen.loadMore();
                last = false;
            }
        }

        public boolean mouseClicked(double p_101706_, double p_101707_, int p_101708_) {
            parent.setSelected(this);
            if (p_101706_ - (double) parent.getRowLeft() <= 32.0D) {
                try {
                    cf.downloadModAndDependencies(mod.id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            return false;
        }
    }
}
