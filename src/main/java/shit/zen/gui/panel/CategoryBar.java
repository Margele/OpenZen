package shit.zen.gui.panel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.ClientBase;
import shit.zen.modules.Category;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Renderer;
import shit.zen.render.TextGlow;
import shit.zen.utils.math.LerpUtil;

public class CategoryBar
extends ClientBase {
    private static final Map<Category, String> CATEGORY_ICONS = new HashMap<>();
    private final Map<Category, Float> hoverAnimations = new HashMap<>();
    private Category selectedCategory = Category.COMBAT;

    public void render(GuiGraphics guiGraphics, int originX, int originY, int mouseX, int mouseY, float scale, float alpha) {
        try {
            int iconSize = (int)(20.0f * scale);
            int iconSpacing = (int)(25.0f * scale);
            int verticalOffset = (int)(80.0f * scale);
            float iconFontSize = 24.0f * scale;
            int horizontalOffset = (int)(420.0f * scale);
            int baseX = originX + horizontalOffset;
            int baseY = originY + verticalOffset - (int)(60.0f * scale);
            Category[] categories = Category.values();
            Renderer.renderConsumer(drawContext -> {
                FontRenderer iconFont = FontPresets.materialIcons(iconFontSize);
                for (int i = 0; i < categories.length; ++i) {
                    Category category = categories[i];
                    String iconString = CATEGORY_ICONS.get(category);
                    if (iconString == null) continue;
                    int iconX = baseX + i * iconSpacing;
                    int iconY = baseY;
                    this.updateCategoryHover(category, iconX, iconY, mouseX, mouseY, iconSize);
                    int categoryColor = this.getCategoryColor(category);
                    if (category == this.selectedCategory) {
                        int glowColor = new Color(255, 255, 255, (int)(150.0f * alpha)).getRGB();
                        TextGlow.drawGlowText(iconString, iconX, iconY, iconFont, this.applyAlpha(categoryColor, alpha), glowColor, 8.0f * scale);
                        continue;
                    }
                    GlHelper.drawText(iconString, iconX, iconY, iconFont, this.applyAlpha(categoryColor, alpha));
                }
            });
        } catch (Exception exception) {
            // empty catch block
        }
    }

    private int applyAlpha(int color, float alpha) {
        int origAlpha = color >> 24 & 0xFF;
        int newAlpha = (int)((float)origAlpha * alpha);
        return newAlpha << 24 | color & 0xFFFFFF;
    }

    private void updateCategoryHover(Category category, int iconX, int iconY, int mouseX, int mouseY, int iconSize) {
        float current = this.hoverAnimations.getOrDefault(category, 0.0f).floatValue();
        boolean hovered = this.isMouseOverCategory(iconX, iconY, mouseX, mouseY, iconSize);
        this.hoverAnimations.put(category, LerpUtil.lerp(current, hovered ? 1.0f : 0.0f, 0.12f));
    }

    private int getCategoryColor(Category category) {
        if (category == this.selectedCategory) {
            return -1;
        }
        float hoverAmount = this.hoverAnimations.getOrDefault(category, 0.0f).floatValue();
        return this.lerpColor(-7829368, -3355444, hoverAmount);
    }

    private int lerpColor(int fromColor, int toColor, float t) {
        float inv = 1.0f - t;
        int aFrom = fromColor >> 24 & 0xFF;
        int rFrom = fromColor >> 16 & 0xFF;
        int gFrom = fromColor >> 8 & 0xFF;
        int bFrom = fromColor & 0xFF;
        int aTo = toColor >> 24 & 0xFF;
        int rTo = toColor >> 16 & 0xFF;
        int gTo = toColor >> 8 & 0xFF;
        int bTo = toColor & 0xFF;
        int a = (int)((float)aFrom * inv + (float)aTo * t);
        int r = (int)((float)rFrom * inv + (float)rTo * t);
        int g = (int)((float)gFrom * inv + (float)gTo * t);
        int b = (int)((float)bFrom * inv + (float)bTo * t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private boolean isMouseOverCategory(int iconX, int iconY, int mouseX, int mouseY, int iconSize) {
        int halfSize = iconSize / 2;
        return mouseX >= iconX && mouseX <= iconX + iconSize && mouseY >= iconY - halfSize && mouseY <= iconY + halfSize;
    }

    public boolean onMouseClick(int originX, int originY, int mouseX, int mouseY, float scale) {
        int iconSize = (int)(20.0f * scale);
        int iconSpacing = (int)(25.0f * scale);
        int verticalOffset = (int)(80.0f * scale);
        int horizontalOffset = (int)(420.0f * scale);
        int baseX = originX + horizontalOffset;
        int baseY = originY + verticalOffset - (int)(60.0f * scale);
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; ++i) {
            Category category = categories[i];
            int iconX = baseX + i * iconSpacing;
            int iconY = baseY;
            if (!this.isMouseOverCategory(iconX, iconY, mouseX, mouseY, iconSize)) continue;
            this.selectedCategory = category;
            return true;
        }
        return false;
    }

    public Category getSelectedCategory() {
        return this.selectedCategory;
    }

    public void setSelectedCategory(Category category) {
        this.selectedCategory = category;
    }

    public boolean isMouseOverAnyCategory(int originX, int originY, int mouseX, int mouseY, float scale) {
        int iconSize = (int)(20.0f * scale);
        int iconSpacing = (int)(25.0f * scale);
        int verticalOffset = (int)(80.0f * scale);
        int horizontalOffset = (int)(420.0f * scale);
        int baseX = originX + horizontalOffset;
        int baseY = originY + verticalOffset - (int)(60.0f * scale);
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; ++i) {
            int iconX = baseX + i * iconSpacing;
            int iconY = baseY;
            if (!this.isMouseOverCategory(iconX, iconY, mouseX, mouseY, iconSize)) continue;
            return true;
        }
        return false;
    }

    public int getTotalWidth(float scale) {
        int iconSize = (int)(20.0f * scale);
        int iconSpacing = (int)(25.0f * scale);
        return (Category.values().length - 1) * iconSpacing + iconSize;
    }

    public int getCategoryHeight(float scale) {
        return (int)(20.0f * scale);
    }

    static {
        CATEGORY_ICONS.put(Category.COMBAT, "\uE074");
        CATEGORY_ICONS.put(Category.MOVEMENT, "\uE511");
        CATEGORY_ICONS.put(Category.PLAYER, "\uE7FD");
        CATEGORY_ICONS.put(Category.RENDER, "\uE8F4");
        CATEGORY_ICONS.put(Category.EXPLOIT, "\uE894");
        CATEGORY_ICONS.put(Category.WORLD, "\uE5D3");
        CATEGORY_ICONS.put(Category.MISC, "\uE24D");
    }
}