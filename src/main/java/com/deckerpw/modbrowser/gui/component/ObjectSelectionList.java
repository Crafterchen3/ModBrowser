package com.deckerpw.modbrowser.gui.component;

import com.deckerpw.modbrowser.Mod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class ObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
    private static final Component USAGE_NARRATION = new TranslatableComponent("narration.selection.usage");
    private boolean inFocus;

    public static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
    protected int itemHeight  = 36;
    private Screen screen;
    private  int selected = Integer.MAX_VALUE;

    public ObjectSelectionList(Minecraft p_94442_, int p_94443_, int p_94444_, int p_94445_, int p_94446_, int p_94447_, Screen screen) {
        super(p_94442_, p_94443_, p_94444_, p_94445_, p_94446_, p_94447_);
        this.screen = screen;
    }

    public boolean changeFocus(boolean p_94449_) {
        if (!this.inFocus && this.getItemCount() == 0) {
            return false;
        } else {
            this.inFocus = !this.inFocus;
            if (this.inFocus && this.getSelected() == null && this.getItemCount() > 0) {
                this.moveSelection(AbstractSelectionList.SelectionDirection.DOWN);
            } else if (this.inFocus && this.getSelected() != null) {
                this.refreshSelection();
            }

            return this.inFocus;
        }
    }

    public void updateNarration(NarrationElementOutput p_169042_) {
        E e = this.getHovered();
        if (e != null) {
            this.narrateListElementPosition(p_169042_.nest(), e);
            e.updateNarration(p_169042_);
        } else {
            E e1 = this.getSelected();
            if (e1 != null) {
                this.narrateListElementPosition(p_169042_.nest(), e1);
                e1.updateNarration(p_169042_);
            }
        }

        if (this.isFocused()) {
            p_169042_.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }

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

    public void setSelected(@Nullable E p_101675_) {
        super.setSelected(p_101675_);
        selected = children().indexOf(p_101675_);
    }

    protected void moveSelection(AbstractSelectionList.SelectionDirection p_101673_) {
        this.moveSelection(p_101673_, (p_101681_) -> !p_101681_.mod.getCompatibility().isCompatible());
    }
    @Override
    @Nullable
    public E getEntryAtPosition(double x, double y) {
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
                E e = this.getEntry(j);
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

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> implements NarrationSupplier {
        public final Mod mod;

        protected Entry(Mod mod) {
            this.mod = mod;
        }

        public boolean changeFocus(boolean p_94452_) {
            return false;
        }

        public abstract Component getNarration();

        public void updateNarration(NarrationElementOutput p_169044_) {
            p_169044_.add(NarratedElementType.TITLE, this.getNarration());
        }
    }
}