package shit.zen.gui;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import shit.zen.ZenClient;
import shit.zen.gui.panel.CategoryBar;
import shit.zen.gui.panel.KeybindOverlay;
import shit.zen.gui.panel.ModuleListPanel;
import shit.zen.gui.panel.ProfileWidget;
import shit.zen.gui.panel.ScaleSwitchOverlay;
import shit.zen.gui.panel.SettingsPanel;
import shit.zen.gui.panel.setting.NumberSettingRenderer;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Renderer;
import shit.zen.render.TextGlow;
import shit.zen.utils.math.LerpUtil;
import shit.zen.utils.render.RenderUtil;

public class PanelClickGui
extends Screen {

    public enum OpenState {
        CLOSED, OPENING, OPEN, CLOSING
    }

    public enum ScaleSwitchState {
        IDLE, FADING_OUT, WAITING, FADING_IN
    }

    public static final class ToastEntry {
        final String message;
        final long createdAt;
        float currentY;
        float targetY;
        float alpha;

        public ToastEntry(String message) {
            this.message = message;
            this.createdAt = System.currentTimeMillis();
            this.currentY = 20.0f;
            this.targetY = 0.0f;
            this.alpha = 0.0f;
        }
    }

    public static PanelClickGui panelClickGui = new PanelClickGui();

    private boolean searchActive = false;
    private boolean searchFocused = false;
    private String searchQuery = "";
    private long searchCursorTime = 0L;
    private final List<PanelClickGui.ToastEntry> toasts = new CopyOnWriteArrayList<>();
    private PanelClickGui.OpenState currentScaleSwitchState = PanelClickGui.OpenState.CLOSED;
    private float openProgress = 0.0f;
    private float currentScale = 1.0f;
    private final ProfileWidget profileWidget;
    @Getter
    private final CategoryBar categoryBar;
    @Getter
    private final ModuleListPanel moduleListPanel;
    @Getter
    private final SettingsPanel settingsPanel;
    public final KeybindOverlay keybindOverlay = new KeybindOverlay();
    private final ScaleSwitchOverlay scaleSwitchOverlay = new ScaleSwitchOverlay();
    private PanelClickGui.ScaleSwitchState currentOpenState = PanelClickGui.ScaleSwitchState.IDLE;
    private long scaleWaitStart = 0L;
    private float targetScale = 1.0f;
    private float panelAlpha = 1.0f;

    protected PanelClickGui() {
        super(Component.nullToEmpty("New Click GUI"));
        this.profileWidget = new ProfileWidget(this::setScale);
        this.categoryBar = new CategoryBar();
        this.moduleListPanel = new ModuleListPanel();
        this.settingsPanel = new SettingsPanel();
    }

    public void setScale(float newScale) {
        if (this.currentOpenState == PanelClickGui.ScaleSwitchState.IDLE && newScale != this.currentScale) {
            this.targetScale = newScale;
            this.currentOpenState = PanelClickGui.ScaleSwitchState.FADING_OUT;
            this.scaleSwitchOverlay.show(this.currentScale, newScale);
        }
    }

    public void init() {
        super.init();
        LerpUtil.reset();
        if (this.currentScaleSwitchState == PanelClickGui.OpenState.CLOSED) {
            this.openProgress = 0.0f;
        }
        this.currentScaleSwitchState = PanelClickGui.OpenState.OPENING;
    }

    private void updateOpenState() {
        if (this.currentScaleSwitchState == PanelClickGui.OpenState.OPENING) {
            this.openProgress = LerpUtil.lerp(this.openProgress, 1.0f, 0.08f);
            if (this.openProgress >= 1.0f) {
                this.currentScaleSwitchState = PanelClickGui.OpenState.OPEN;
            }
        } else if (this.currentScaleSwitchState == PanelClickGui.OpenState.CLOSING) {
            this.openProgress = LerpUtil.lerp(this.openProgress, 0.0f, 0.1f);
            if (this.openProgress <= 0.0f) {
                this.currentScaleSwitchState = PanelClickGui.OpenState.CLOSED;
                if (ZenClient.isReady()) {
                    ZenClient.instance.getConfigManager().saveAll();
                }
                this.minecraft.setScreen(null);
            }
        }
    }

    private float easeOutCubic(float t) {
        return (float)(1.0 - Math.pow(1.0f - t, 3.0));
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        LerpUtil.update();
        this.updateOpenState();
        if (this.currentScaleSwitchState == PanelClickGui.OpenState.CLOSED && this.openProgress <= 0.0f) {
            return;
        }
        float eased = this.easeOutCubic(this.openProgress);
        float scaleFactor = 0.98f + 0.02f * eased;
        guiGraphics.fill(0, 0, this.width, this.height, new Color(0, 0, 0, (int)(80.0f * eased)).getRGB());
        this.updateScaleSwitchState();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)centerX, (float)centerY, 0.0f);
        guiGraphics.pose().scale(scaleFactor, scaleFactor, 1.0f);
        guiGraphics.pose().translate((float)(-centerX), (float)(-centerY), 0.0f);
        float overlayScale = this.currentOpenState == PanelClickGui.ScaleSwitchState.FADING_OUT
                || this.currentOpenState == PanelClickGui.ScaleSwitchState.WAITING
                ? this.targetScale : this.currentScale;
        int panelWidth = (int)(600.0f * this.currentScale);
        int panelHeight = (int)(400.0f * this.currentScale);
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        float effectiveAlpha = this.panelAlpha * eased;
        if (effectiveAlpha > 0.005f) {
            this.drawPanelGlow(guiGraphics, panelX, panelY, partialTicks, effectiveAlpha);
            this.categoryBar.render(guiGraphics, panelX, panelY, mouseX, mouseY, this.currentScale, effectiveAlpha);
            this.moduleListPanel.render(guiGraphics, panelX, panelY, mouseX, mouseY, this.getSelectedCategory(), this.currentScale, effectiveAlpha);
            this.settingsPanel.render(guiGraphics, panelX, panelY, mouseX, mouseY, this.moduleListPanel.getHoveredModule(), this.currentScale, effectiveAlpha);
            this.profileWidget.render(guiGraphics, panelX, panelY, mouseX, mouseY, this.currentScale, effectiveAlpha);
            this.drawSearchBar(guiGraphics, panelX, panelY, mouseX, mouseY, effectiveAlpha);
            this.drawToasts(guiGraphics, panelX, panelY, effectiveAlpha);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.pose().popPose();
        this.keybindOverlay.render(guiGraphics, this.width, this.height, overlayScale);
        this.scaleSwitchOverlay.render(guiGraphics, this.width, this.height, overlayScale);
    }

    private void updateScaleSwitchState() {
        switch (this.currentOpenState) {
            case FADING_OUT: {
                this.panelAlpha = LerpUtil.lerp(this.panelAlpha, 0.0f, 0.08f);
                if (this.panelAlpha <= 0.0f) {
                    this.currentOpenState = PanelClickGui.ScaleSwitchState.WAITING;
                    this.scaleWaitStart = System.currentTimeMillis();
                }
                break;
            }
            case WAITING: {
                if (System.currentTimeMillis() - this.scaleWaitStart > 3000L) {
                    this.currentScale = this.targetScale;
                    this.currentOpenState = PanelClickGui.ScaleSwitchState.FADING_IN;
                    this.scaleSwitchOverlay.hide();
                }
                break;
            }
            case FADING_IN: {
                this.panelAlpha = LerpUtil.lerp(this.panelAlpha, 1.0f, 0.08f);
                if (this.panelAlpha >= 1.0f && this.scaleSwitchOverlay.isFullyHidden()) {
                    this.currentOpenState = PanelClickGui.ScaleSwitchState.IDLE;
                }
            }
        }
    }

    private void drawPanelGlow(GuiGraphics guiGraphics, int panelX, int panelY, float partialTicks, float alpha) {
        int panelWidth = (int)(600.0f * this.currentScale);
        int panelHeight = (int)(400.0f * this.currentScale);
        TextGlow.drawBackground(guiGraphics.pose(), panelX, panelY, panelWidth, panelHeight, 12.0f * this.currentScale, alpha);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.openProgress < 1.0f) {
            return true;
        }
        if (this.keybindOverlay.isVisible()) {
            if (button == 0) {
                this.keybindOverlay.cancel();
            }
            return true;
        }
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = (int)(600.0f * this.currentScale);
        int panelHeight = (int)(400.0f * this.currentScale);
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        if (this.profileWidget.isPopupOpen() && this.profileWidget.onMouseClick(panelX, panelY, (int)mouseX, (int)mouseY, this.currentScale)) {
            return true;
        }
        int searchWidth = (int)(200.0f * this.currentScale);
        int searchHeight = (int)(20.0f * this.currentScale);
        int searchX = panelX + (panelWidth - searchWidth) / 2;
        int searchY = panelY + panelHeight + (int)(15.0f * this.currentScale);
        boolean overSearch = mouseX >= (double)searchX && mouseX <= (double)(searchX + searchWidth)
                && mouseY >= (double)searchY && mouseY <= (double)(searchY + searchHeight);
        if (overSearch) {
            if (!this.searchActive) {
                this.searchActive = true;
                this.searchQuery = "";
                this.moduleListPanel.setSearchQuery("");
            }
            this.searchFocused = true;
            this.searchCursorTime = System.currentTimeMillis();
            return true;
        }
        this.searchFocused = false;
        boolean handled = false;
        if (button == 0 || button == 1 || button == 2) {
            if (button == 0 && this.categoryBar.onMouseClick(panelX, panelY, (int)mouseX, (int)mouseY, this.currentScale)) {
                if (this.searchActive) {
                    this.searchActive = false;
                    this.searchFocused = false;
                    this.searchQuery = "";
                    this.moduleListPanel.setSearchQuery(null);
                }
                handled = true;
            }
            if (!handled && this.moduleListPanel.onMouseClick(panelX, panelY, (int)mouseX, (int)mouseY, this.getSelectedCategory(), button, this.currentScale)) {
                handled = true;
            }
            if (!handled && this.profileWidget.onMouseClick(panelX, panelY, (int)mouseX, (int)mouseY, this.currentScale)) {
                handled = true;
            }
            if (!handled && (button == 0 || button == 1) && this.settingsPanel.onMouseClick(panelX, panelY, (int)mouseX, (int)mouseY, button, this.currentScale)) {
                handled = true;
            }
        }
        if (!handled) {
            NumberSettingRenderer.clearEditing();
        }
        return handled || super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.openProgress < 1.0f) {
            return true;
        }
        this.moduleListPanel.onMouseRelease();
        this.settingsPanel.onMouseRelease(mouseX, mouseY, button);
        this.profileWidget.onMouseRelease();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (this.openProgress < 1.0f) {
            return true;
        }
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - (int)(600.0f * this.currentScale) / 2;
        int panelY = centerY - (int)(400.0f * this.currentScale) / 2;
        if (this.moduleListPanel.isMouseOverPanel(panelX, panelY, (int)mouseX, (int)mouseY, this.currentScale)) {
            this.moduleListPanel.onScroll(scrollDelta, this.currentScale);
            return true;
        }
        if (this.settingsPanel.isMouseOverPanel(panelX, panelY, (int)mouseX, (int)mouseY, this.currentScale)) {
            this.settingsPanel.onScroll(scrollDelta, this.currentScale);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.openProgress < 1.0f) {
            return true;
        }
        this.moduleListPanel.onMouseDrag(mouseX, mouseY, this.currentScale);
        this.settingsPanel.onMouseDrag(mouseX, mouseY, this.currentScale);
        this.profileWidget.onMouseDrag((int)mouseX, (int)mouseY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.keybindOverlay.onKeyPress(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.searchActive) {
            if (keyCode == 256) {
                this.searchActive = false;
                this.searchFocused = false;
                this.searchQuery = "";
                this.moduleListPanel.setSearchQuery(null);
                return true;
            }
            if (this.searchFocused) {
                this.searchCursorTime = System.currentTimeMillis();
                if (keyCode == 259) {
                    if (!this.searchQuery.isEmpty()) {
                        this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
                        this.moduleListPanel.setSearchQuery(this.searchQuery);
                    }
                    return true;
                }
            }
        }
        if (NumberSettingRenderer.onKeyPress(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == 256 && !this.keybindOverlay.isVisible() && !this.searchActive) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char c, int modifiers) {
        if (this.searchActive && this.searchFocused) {
            this.searchCursorTime = System.currentTimeMillis();
            this.searchQuery = this.searchQuery + c;
            this.moduleListPanel.setSearchQuery(this.searchQuery);
            return true;
        }
        if (NumberSettingRenderer.onCharTyped(c)) {
            return true;
        }
        return super.charTyped(c, modifiers);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        if (this.currentScaleSwitchState != PanelClickGui.OpenState.CLOSING) {
            this.currentScaleSwitchState = PanelClickGui.OpenState.CLOSING;
        }
    }

    public Category getSelectedCategory() {
        return this.categoryBar.getSelectedCategory();
    }

    private void drawSearchBar(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY, float alpha) {
        try {
            int panelWidth = (int)(600.0f * this.currentScale);
            int panelHeight = (int)(400.0f * this.currentScale);
            int searchWidth = (int)(200.0f * this.currentScale);
            int searchHeight = (int)(20.0f * this.currentScale);
            int searchX = panelX + (panelWidth - searchWidth) / 2;
            int searchY = panelY + panelHeight + (int)(15.0f * this.currentScale);
            TextGlow.drawBackground(guiGraphics.pose(), searchX, searchY, searchWidth, searchHeight, 8.0f * this.currentScale, alpha);
            Renderer.renderConsumer(drawContext -> {
                FontRenderer iconFont = FontPresets.materialIcons(20.0f * this.currentScale);
                String iconText = "\uE8B6";
                float iconX = searchX + 12.0f * this.currentScale;
                // 已修复：添加 this.currentScale 乘数
                float iconY = searchY + searchHeight / 2.0f + iconFont.getMetrics().capHeight() / 2.0f - 12.0f * this.currentScale + (10.0f * this.currentScale);
                int iconColor = new Color(255, 255, 255, (int)(180.0f * alpha)).getRGB();
                TextGlow.drawGlowText(iconText, iconX, iconY, iconFont, 0xffdddddd, iconColor, 12.0f * this.currentScale);
                FontRenderer queryFont = FontPresets.axiformaRegular(16.0f * this.currentScale);
                if (!this.searchActive && this.searchQuery.isEmpty()) {
                    FontRenderer placeholderFont = FontPresets.axiformaBold(14.0f * this.currentScale);
                    String placeholder = "search";
                    float placeholderWidth = GlHelper.getStringWidth(placeholder, placeholderFont);
                    float placeholderX = searchX + (searchWidth - placeholderWidth) / 2.0f;
                    // 已修复：添加 this.currentScale 乘数
                    float placeholderY = searchY + searchHeight / 2.0f + placeholderFont.getMetrics().capHeight() / 2.0f - 8.0f * this.currentScale + (5.0f * this.currentScale);
                    TextGlow.drawGlowText(placeholder, placeholderX, placeholderY, placeholderFont, 0xffcccccc,
                            new Color(255, 255, 255, (int)(130.0f * alpha)).getRGB(), 10.0f * this.currentScale);
                } else {
                    float queryX = searchX + 35.0f * this.currentScale;
                    // 已修复：添加 this.currentScale 乘数
                    float queryY = searchY + searchHeight / 2.0f + queryFont.getMetrics().capHeight() / 2.0f - 9.0f * this.currentScale + (6.0f * this.currentScale);
                    int queryColor = new Color(255, 255, 255, (int)(120.0f * alpha)).getRGB();
                    TextGlow.drawGlowText(this.searchQuery, queryX, queryY, queryFont, -1, queryColor, 8.0f * this.currentScale);
                    if (this.searchFocused) {
                        long sinceCursor = System.currentTimeMillis() - this.searchCursorTime;
                        float blinkAmount = (float)(Math.sin(sinceCursor / 200.0) * 0.5 + 0.5);
                        int cursorColor = (int)(blinkAmount * alpha * 255.0f) << 24 | 0xFFFFFF;
                        float cursorX = queryX + GlHelper.getStringWidth(this.searchQuery, queryFont) + 2.0f * this.currentScale;
                        float cursorHeight = queryFont.getMetrics().capHeight();
                        RenderUtil.drawFilledRect(guiGraphics.pose(), cursorX,
                                queryY - cursorHeight + 3.0f * this.currentScale,
                                this.currentScale,
                                cursorHeight + 2 * this.currentScale,
                                cursorColor);
                    }
                }
            });
        } catch (Exception exception) {
            // empty catch block
        }
    }

    public void addToast(String message) {
        for (PanelClickGui.ToastEntry toast : this.toasts) {
            toast.targetY -= 20.0f * this.currentScale;
        }
        this.toasts.add(new PanelClickGui.ToastEntry(message));
    }

    public void selectModule(Module module) {
        this.keybindOverlay.startBinding(module);
    }

    private void drawToasts(GuiGraphics guiGraphics, int panelX, int panelY, float alpha) {
        if (this.toasts.isEmpty()) {
            return;
        }
        try {
            Renderer.renderConsumer(drawContext -> {
                FontRenderer toastFont = FontPresets.axiformaBold(18.0f * this.currentScale);
                for (PanelClickGui.ToastEntry toast : this.toasts) {
                    long elapsed = System.currentTimeMillis() - toast.createdAt;
                    toast.currentY = LerpUtil.smoothLerp(toast.currentY, toast.targetY, 0.2f);
                    float targetAlpha = 0.0f;
                    if (elapsed < 2000L) {
                        targetAlpha = 1.0f;
                    } else if (elapsed < 2500L) {
                        long fadeMs = elapsed - 2000L;
                        targetAlpha = 1.0f - (float)fadeMs / 500.0f;
                    }
                    toast.alpha = LerpUtil.smoothLerp(toast.alpha, targetAlpha, 0.25f);
                    if (toast.alpha < 0.01f && elapsed > 2000L) {
                        this.toasts.remove(toast);
                        continue;
                    }
                    float toastWidth = GlHelper.getStringWidth(toast.message, toastFont);
                    int panelWidth = (int)(600.0f * this.currentScale);
                    float toastX = (float)panelX + ((float)panelWidth - toastWidth) / 2.0f;
                    float toastY = (float)(panelY - 25) + toast.currentY;
                    int textAlpha = (int)(255.0f * toast.alpha * alpha);
                    int textColor = textAlpha << 24 | 0xFFFFFF;
                    int glowAlpha = (int)(120.0f * toast.alpha * alpha);
                    int glowColor = glowAlpha << 24 | 0xFFFFFF;
                    // 已修复：将 8.0f * scale 改为 8.0f * this.currentScale
                    TextGlow.drawGlowText(toast.message, toastX, toastY + 6.0f * this.currentScale, toastFont, textColor, glowColor, 8.0f * this.currentScale);
                }
            });
        } catch (Exception exception) {
            // empty catch block
        }
    }
}