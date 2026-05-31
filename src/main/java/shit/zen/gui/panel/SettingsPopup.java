package shit.zen.gui.panel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.ClientBase;
import shit.zen.ZenClient;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Rectangle;
import shit.zen.render.Renderer;
import shit.zen.render.TextGlow;
import shit.zen.utils.math.LerpUtil;
import shit.zen.utils.render.RenderUtil;

public class SettingsPopup
extends ClientBase {
    private boolean isOpen = false;
    private boolean isDragging = false;
    private int lastDragX = 0;
    private int lastDragY = 0;
    private int offsetX = 0;
    private int offsetY = 0;
    private float openAlpha = 0.0f;
    private float closeButtonHoverAlpha = 0.0f;
    private boolean isCloseButtonHovered = false;
    private final Map<String, Boolean> dropdownOpen = new HashMap<>();
    private final Map<String, Float> dropdownAlpha = new HashMap<>();
    private final Map<String, Map<String, Float>> dropdownItemHover = new HashMap<>();
    private static final String[] LANGUAGES = new String[]{"English", "Chinese"};
    private String selectedLanguage = "English";
    private static final String[] SCALES = new String[]{"50%", "75%", "100%", "125%", "150%"};
    private String selectedScale = "100%";
    private static final Color POPUP_BG_COLOR = new Color(20, 20, 24, 230);
    private final Consumer<Float> scaleChangeCallback;

    public SettingsPopup(Consumer<Float> scaleChangeCallback) {
        this.scaleChangeCallback = scaleChangeCallback;
        this.dropdownOpen.put("language", false);
        this.dropdownOpen.put("scale", false);
        this.dropdownAlpha.put("language", 0.0f);
        this.dropdownAlpha.put("scale", 0.0f);
        this.dropdownItemHover.put("language", new HashMap<>());
        this.dropdownItemHover.put("scale", new HashMap<>());
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float scale, float alpha) {
        this.updatePopupPosition(mouseX, mouseY, scale);
        this.updateOpenAlpha();
        this.updateCloseButtonHover();
        this.updateDropdownAlpha();
        if (this.openAlpha > 0.01f) {
            this.clampPopupPosition(scale);
            this.renderPopupContent(guiGraphics, mouseX, mouseY, scale, alpha);
        }
    }

    private void renderPopupContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float scale, float alpha) {
        int popupWidth = (int)(220.0f * scale);
        int popupHeight = this.calculatePopupHeight(scale);
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int popupX = (screenWidth - popupWidth) / 2 + this.offsetX;
        int popupY = (screenHeight - (int)(200.0f * scale)) / 2 + this.offsetY;
        float effectiveAlpha = this.openAlpha * alpha;
        int alphaByte = (int)(255.0f * effectiveAlpha);
        TextGlow.drawBackground(guiGraphics.pose(), popupX, popupY, popupWidth, popupHeight, 12.0f * scale, effectiveAlpha);
        Renderer.renderConsumer((drawContext -> this.drawPopupBody(drawContext, guiGraphics, popupX, popupY, mouseX, mouseY, popupHeight, alphaByte, scale, popupWidth)));
    }

    private void drawPopupBody(DrawContext drawContext, GuiGraphics guiGraphics, int popupX, int popupY, int mouseX, int mouseY, int popupHeight, int alphaByte, float scale, int popupWidth) {
        int whiteColor = alphaByte << 24 | 0xFFFFFF;
        FontRenderer iconFont = FontPresets.materialIcons(18.0f * scale);
        GlHelper.drawText("\uE8B8", (float)popupX + 15.0f * scale, (float)popupY + 16.0f * scale, iconFont, whiteColor);
        FontRenderer titleFont = FontPresets.museoSans(22.0f * scale);
        String title = "ZENLESS.ZONE";
        float titleWidth = GlHelper.getStringWidth(title, titleFont);
        GlHelper.drawText(title, (float)popupX + ((float)popupWidth - titleWidth) / 2.0f, (float)popupY + 35.0f * scale, titleFont, whiteColor);
        this.drawCloseButton(popupX, popupY, iconFont, alphaByte, scale, popupWidth);
        FontRenderer labelFont = FontPresets.axiformaRegular(13.0f * scale);
        FontRenderer valueFont = FontPresets.axiformaRegular(13.0f * scale);
        int labelColor = alphaByte << 24 | 0xAAAAAA;
        int valueColor = alphaByte << 24 | 0xFFFFFF;
        int rowHeight = (int)(18.0f * scale);
        int rowY = (int)((float)popupY + 65.0f * scale);
        int rightEdge = (int)((float)(popupX + popupWidth) - 15.0f * scale);
        GlHelper.drawText("Username:", (float)popupX + 15.0f * scale, rowY, labelFont, labelColor);
        String userId = this.getUserId();
        float userIdWidth = GlHelper.getStringWidth(userId, valueFont);
        GlHelper.drawText(userId, (float)rightEdge - userIdWidth, rowY, valueFont, valueColor);
        GlHelper.drawText("Branch:", (float)popupX + 15.0f * scale, rowY += rowHeight, labelFont, labelColor);
        String userRole = this.getUserRole();
        float roleWidth = GlHelper.getStringWidth(userRole, valueFont);
        GlHelper.drawText(userRole, (float)rightEdge - roleWidth, rowY, valueFont, valueColor);
        GlHelper.drawText("Updated:", (float)popupX + 15.0f * scale, rowY += rowHeight, labelFont, labelColor);
        String updatedDate = "Aug 4 2025";
        float dateWidth = GlHelper.getStringWidth(updatedDate, valueFont);
        GlHelper.drawText(updatedDate, (float)rightEdge - dateWidth, rowY, valueFont, valueColor);
        rowY += rowHeight;
        rowY = (int)((float)rowY + 8.0f * scale);
        rowY += this.drawDropdown(drawContext, guiGraphics, "Language", this.selectedLanguage, LANGUAGES, "language", popupX, rowY, mouseX, mouseY, this.openAlpha, scale, popupWidth);
        rowY = (int)((float)rowY + 8.0f * scale);
        this.drawDropdown(drawContext, guiGraphics, "Menu Scale", this.selectedScale, SCALES, "scale", popupX, rowY, mouseX, mouseY, this.openAlpha, scale, popupWidth);
        FontRenderer footerFont = FontPresets.axiformaRegular(12.0f * scale);
        String footer = "7unknown \u00a9 2024-2025";
        float footerWidth = GlHelper.getStringWidth(footer, footerFont);
        GlHelper.drawText(footer, (float)popupX + ((float)popupWidth - footerWidth) / 2.0f, (float)(popupY + popupHeight) - 15.0f * scale, footerFont, labelColor);
    }

    private void drawCloseButton(int popupX, int popupY, FontRenderer iconFont, int alphaByte, float scale, int popupWidth) {
        float btnX = (float)(popupX + popupWidth) - 25.0f * scale;
        float btnY = (float)popupY + 16.0f * scale;
        Color colorFrom = new Color(255, 255, 255);
        Color colorTo = new Color(255, 255, 255);
        int r = (int)((float)colorFrom.getRed() + (float)(colorTo.getRed() - colorFrom.getRed()) * this.closeButtonHoverAlpha);
        int g = (int)((float)colorFrom.getGreen() + (float)(colorTo.getGreen() - colorFrom.getGreen()) * this.closeButtonHoverAlpha);
        int b = (int)((float)colorFrom.getBlue() + (float)(colorTo.getBlue() - colorFrom.getBlue()) * this.closeButtonHoverAlpha);
        int textColor = alphaByte << 24 | r << 16 | g << 8 | b;
        int glowAlpha = (int)(180.0f * this.closeButtonHoverAlpha * this.openAlpha);
        int glowColor = new Color(r, g, b, glowAlpha).getRGB();
        TextGlow.drawGlowText("", btnX, btnY, iconFont, textColor, glowColor, 10.0f * scale);
    }

    private int drawDropdown(DrawContext drawContext, GuiGraphics guiGraphics, String label, String selectedValue, String[] items, String key, int popupX, int rowY, int mouseX, int mouseY, float openAlpha, float scale, int popupWidth) {
        FontRenderer labelFont = FontPresets.axiformaRegular(13.0f * scale);
        FontRenderer valueFont = FontPresets.axiformaRegular(13.0f * scale);
        int labelColor = this.applyAlpha(new Color(0xAAAAAA).getRGB(), openAlpha);
        int valueColor = this.applyAlpha(new Color(0xFFFFFF).getRGB(), openAlpha);
        
        int dropdownWidth = (int)(90.0f * scale);
        int dropdownX = (int)((float)(popupX + popupWidth - dropdownWidth) - 15.0f * scale);
        int dropdownHeaderHeight = (int)(20.0f * scale);
        int itemHeight = (int)(18.0f * scale);
        
        // --- 核心修复：标签左侧文字基于 CapHeight 垂直居中 ---
        float labelY = rowY + (dropdownHeaderHeight - labelFont.getMetrics().capHeight()) / 2.0f;
        GlHelper.drawText(label, (float)popupX + 15.0f * scale, labelY, labelFont, labelColor);
        
        float openFactor = this.dropdownAlpha.getOrDefault(key, 0.0f).floatValue();
        String[] filteredItems = this.filterDropdownItems(items, selectedValue);
        int expandedHeight = (int)((float)(filteredItems.length * itemHeight) * openFactor);
        RenderUtil.drawRoundedRect(guiGraphics.pose(), dropdownX, rowY, dropdownWidth, dropdownHeaderHeight + expandedHeight, 4.0f * scale, this.applyAlpha(POPUP_BG_COLOR.getRGB(), openAlpha));
        
        // --- 核心修复：选择框内部文字基于 CapHeight 垂直居中（抛弃无脑的常量加减偏移） ---
        float valueX = (float)dropdownX + 8.0f * scale;
        float valueY = rowY + (dropdownHeaderHeight - valueFont.getMetrics().capHeight()) / 2.0f;
        GlHelper.drawText(selectedValue, valueX, valueY, valueFont, valueColor);
        
        // --- 核心修复：展开的小箭头基于 CapHeight 垂直居中（抛弃 +7.0f 的不缩放硬编码偏移） ---
        FontRenderer arrowFont = FontPresets.materialIcons(18.0f * scale);
        String arrowIcon = "";
        float arrowX = (float)(dropdownX + dropdownWidth) - 18.0f * scale;
        float arrowY = rowY + (dropdownHeaderHeight - arrowFont.getMetrics().capHeight()) / 2.0f;
        GlHelper.drawText(arrowIcon, arrowX, arrowY, arrowFont, valueColor);
        
        if (openFactor > 0.01f) {
            drawContext.save();
            drawContext.clip(Rectangle.ofXYWH(dropdownX, rowY + dropdownHeaderHeight, dropdownWidth, expandedHeight));
            Map<String, Float> itemHovers = this.dropdownItemHover.get(key);
            int itemY = rowY + dropdownHeaderHeight;
            for (String item : filteredItems) {
                boolean hovered = this.isPointInBounds(mouseX, mouseY, dropdownX, itemY, dropdownWidth, itemHeight);
                this.updateItemHover(itemHovers, item, hovered);
                float hoverAmount = itemHovers.getOrDefault(item, 0.0f);
                float itemTextX = (float)dropdownX + 8.0f * scale;
                // --- 核心修复：展开列表里的每一项文字也基于 CapHeight 完美居中 ---
                float itemTextY = itemY + (itemHeight - valueFont.getMetrics().capHeight()) / 2.0f;
                int itemColor = this.applyAlpha(valueColor, openFactor);
                float glowAmount = hoverAmount * openFactor;
                if (glowAmount > 0.01f) {
                    int glowColor = new Color(1.0f, 1.0f, 1.0f, glowAmount).getRGB();
                    TextGlow.drawGlowText(item, itemTextX, itemTextY, valueFont, itemColor, glowColor, 8.0f * scale);
                } else {
                    GlHelper.drawText(item, itemTextX, itemTextY, valueFont, itemColor);
                }
                itemY += itemHeight;
            }
            drawContext.restore();
        }
        return dropdownHeaderHeight + expandedHeight;
    }

    private String getUserId() {
        return ZenClient.username != null && !ZenClient.username.isEmpty() ? ZenClient.username : "Unknown";
    }

    private String getUserRole() {
        try {
            if (false) {
                List<String> roles = new ArrayList<>();
                if (roles.contains("ROLE_OWNER")) return "Premium";
                if (roles.contains("ROLE_ADMIN")) return "Admin";
                if (roles.contains("ROLE_BETA")) return "Beta";
                return roles.get(0).replace("ROLE_", "");
            }
        } catch (Exception exception) {
            // empty catch block
        }
        return "Admin";
    }

    public boolean onMouseClick(int mouseX, int mouseY, float scale) {
        int screenHeight;
        int popupY;
        int popupWidth = (int)(220.0f * scale);
        int popupHeight = this.calculatePopupHeight(scale);
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int popupX = (screenWidth - popupWidth) / 2 + this.offsetX;
        if (this.isMouseOverCloseButton(mouseX, mouseY, popupX, popupY = ((screenHeight = mc.getWindow().getGuiScaledHeight()) - (int)(200.0f * scale)) / 2 + this.offsetY, scale, popupWidth)) {
            this.toggleOpen();
            return true;
        }
        if (this.isDragging) {
            return true;
        }
        if (this.isMouseInRect(mouseX, mouseY, popupX, popupY, scale, popupWidth)) {
            this.beginDrag(mouseX, mouseY);
            return true;
        }
        int dropdownWidth = (int)(90.0f * scale);
        int dropdownX = (int)((float)(popupX + popupWidth - dropdownWidth) - 15.0f * scale);
        int langRowY = (int)((float)popupY + 127.0f * scale);
        boolean langHandled = this.handleDropdownClick(mouseX, mouseY, dropdownX, langRowY, dropdownWidth, LANGUAGES, this.selectedLanguage, "language", value -> {
            this.selectedLanguage = value;
        }, scale);
        float langExpanded = (float)this.filterDropdownItems(LANGUAGES, this.selectedLanguage).length * (18.0f * scale) * this.dropdownAlpha.getOrDefault("language", 0.0f);
        int scaleRowY = (int)((float)langRowY + 20.0f * scale + langExpanded + 8.0f * scale);
        boolean scaleHandled = this.handleDropdownClick(mouseX, mouseY, dropdownX, scaleRowY, dropdownWidth, SCALES, this.selectedScale, "scale", value -> {
            this.selectedScale = value;
            try {
                float parsed = Float.parseFloat(value.replace("%", "")) / 100.0f;
                this.scaleChangeCallback.accept(parsed);
            } catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }, scale);
        boolean withinPopup = this.isPointInBounds(mouseX, mouseY, popupX, popupY, popupWidth, popupHeight);
        if (langHandled || scaleHandled) {
            return true;
        }
        if (withinPopup) {
            this.dropdownOpen.put("language", false);
            this.dropdownOpen.put("scale", false);
            return true;
        }
        return false;
    }

    private boolean handleDropdownClick(int mouseX, int mouseY, int dropdownX, int dropdownY, int dropdownWidth, String[] items, String selectedValue, String key, Consumer<String> onSelect, float scale) {
        boolean open = this.dropdownOpen.getOrDefault(key, false);
        int itemHeight = (int)(18.0f * scale);
        int headerHeight = (int)(20.0f * scale);
        if (this.isPointInBounds(mouseX, mouseY, dropdownX, dropdownY, dropdownWidth, headerHeight)) {
            this.dropdownOpen.put(key, !open);
            this.dropdownOpen.keySet().stream().filter(otherKey -> !otherKey.equals(key)).forEach(otherKey -> this.dropdownOpen.put(otherKey, false));
            return true;
        }
        if (open) {
            String[] filtered = this.filterDropdownItems(items, selectedValue);
            for (int i = 0; i < filtered.length; ++i) {
                if (!this.isPointInBounds(mouseX, mouseY, dropdownX, dropdownY + headerHeight + i * itemHeight, dropdownWidth, itemHeight)) continue;
                onSelect.accept(filtered[i]);
                this.dropdownOpen.put(key, false);
                return true;
            }
        }
        return false;
    }

    private boolean isMouseInRect(int mouseX, int mouseY, int popupX, int popupY, float scale, int popupWidth) {
        float closeBtnX = (float)(popupX + popupWidth) - 25.0f * scale;
        boolean overCloseBtn = (float)mouseX >= closeBtnX - 10.0f * scale && (float)mouseX <= closeBtnX + 15.0f * scale;
        return mouseX >= popupX && mouseX <= popupX + popupWidth && mouseY >= popupY && (float)mouseY <= (float)popupY + 30.0f * scale && !overCloseBtn;
    }

    private boolean isMouseOverCloseButton(int mouseX, int mouseY, int popupX, int popupY, float scale, int popupWidth) {
        float closeBtnX = (float)(popupX + popupWidth) - 25.0f * scale;
        float closeBtnY = (float)popupY + 16.0f * scale;
        return (float)mouseX >= closeBtnX - 10.0f * scale && (float)mouseX <= closeBtnX + 15.0f * scale && (float)mouseY >= closeBtnY - 10.0f * scale && (float)mouseY <= closeBtnY + 10.0f * scale;
    }

    private void beginDrag(int mouseX, int mouseY) {
        this.isDragging = true;
        this.lastDragX = mouseX;
        this.lastDragY = mouseY;
    }

    public void onMouseDrag(int mouseX, int mouseY) {
        if (this.isDragging) {
            this.offsetX += mouseX - this.lastDragX;
            this.offsetY += mouseY - this.lastDragY;
            this.lastDragX = mouseX;
            this.lastDragY = mouseY;
        }
    }

    public void stopDrag() {
        this.isDragging = false;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public void toggleOpen() {
        this.isOpen = !this.isOpen;
    }

    private void updateOpenAlpha() {
        if (this.isOpen) {
            this.openAlpha = LerpUtil.lerp(this.openAlpha, 1.0f, 0.1f);
        } else {
            this.openAlpha = LerpUtil.lerp(this.openAlpha, 0.0f, 0.1f);
            if (this.openAlpha < 0.01f) {
                this.dropdownOpen.put("language", false);
                this.dropdownOpen.put("scale", false);
            }
        }
    }

    private void updateDropdownAlpha() {
        for (String key : this.dropdownOpen.keySet()) {
            boolean open = this.dropdownOpen.getOrDefault(key, false);
            float current = this.dropdownAlpha.getOrDefault(key, 0.0f).floatValue();
            float target = open ? 1.0f : 0.0f;
            current = Math.abs(current - target) > 0.01f ? LerpUtil.smoothLerp(current, target, 0.22f) : target;
            this.dropdownAlpha.put(key, current);
        }
    }

    private void updatePopupPosition(int mouseX, int mouseY, float scale) {
        if (this.isOpen) {
            int popupWidth = (int)(220.0f * scale);
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int popupX = (screenWidth - popupWidth) / 2 + this.offsetX;
            int popupY = (screenHeight - (int)(200.0f * scale)) / 2 + this.offsetY;
            this.isCloseButtonHovered = this.isMouseOverCloseButton(mouseX, mouseY, popupX, popupY, scale, popupWidth);
        } else {
            this.isCloseButtonHovered = false;
        }
    }

    private void updateCloseButtonHover() {
        this.closeButtonHoverAlpha = this.isCloseButtonHovered ? LerpUtil.lerp(this.closeButtonHoverAlpha, 1.0f, 0.16f) : LerpUtil.lerp(this.closeButtonHoverAlpha, 0.0f, 0.16f);
    }

    private void updateItemHover(Map<String, Float> hoverMap, String key, boolean hovered) {
        float current = hoverMap.getOrDefault(key, 0.0f).floatValue();
        float target = hovered ? 1.0f : 0.0f;
        current = Math.abs(current - target) > 0.01f ? LerpUtil.smoothLerp(current, target, 0.28f) : target;
        hoverMap.put(key, current);
    }

    private String[] filterDropdownItems(String[] items, String selectedValue) {
        return Stream.of((Object[])items).filter(item -> !Objects.equals(item, selectedValue)).toArray(String[]::new);
    }

    private boolean isPointInBounds(int pointX, int pointY, int boxX, int boxY, int boxWidth, int boxHeight) {
        return pointX >= boxX && pointX <= boxX + boxWidth && pointY >= boxY && pointY <= boxY + boxHeight;
    }

    private int applyAlpha(int color, float alpha) {
        int origAlpha = color >> 24 & 0xFF;
        int newAlpha = (int)((float)origAlpha * alpha);
        return newAlpha << 24 | color & 0xFFFFFF;
    }

    private int calculatePopupHeight(float scale) {
        float baseHeight = 200.0f * scale;
        float itemHeight = 18.0f * scale;
        float langExpanded = (float)this.filterDropdownItems(LANGUAGES, this.selectedLanguage).length * itemHeight * this.dropdownAlpha.getOrDefault("language", 0.0f).floatValue();
        float scaleExpanded = (float)this.filterDropdownItems(SCALES, this.selectedScale).length * itemHeight * this.dropdownAlpha.getOrDefault("scale", 0.0f).floatValue();
        return (int)(baseHeight + langExpanded + scaleExpanded);
    }

    private void clampPopupPosition(float scale) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int popupHeight = this.calculatePopupHeight(scale);
        int popupWidth = (int)(220.0f * scale);
        int maxOffsetX = (screenWidth - popupWidth) / 2;
        int minOffsetX = -(screenWidth - popupWidth) / 2;
        int maxOffsetY = (screenHeight - popupHeight) / 2;
        int minOffsetY = -(screenHeight - (int)(200.0f * scale)) / 2;
        this.offsetX = Math.max(minOffsetX, Math.min(this.offsetX, maxOffsetX));
        this.offsetY = Math.max(minOffsetY, Math.min(this.offsetY, maxOffsetY));
    }
}
