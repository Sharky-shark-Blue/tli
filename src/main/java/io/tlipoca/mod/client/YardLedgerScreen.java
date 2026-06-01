package io.tlipoca.mod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class YardLedgerScreen extends Screen {
    private static final int OVERLAY = 0x66000000;
    private static final int BACKGROUND = 0xF20A0614;
    private static final int PANEL = 0xE70F071B;
    private static final int PANEL_SOFT = 0xB9160B28;
    private static final int PANEL_DARK = 0xD9080410;
    private static final int BORDER = 0xFF5D3286;
    private static final int BORDER_SOFT = 0x994B2B72;
    private static final int PURPLE = 0xFFB25CFF;
    private static final int PURPLE_DARK = 0xFF61308D;
    private static final int TEXT = 0xFFE8DEF0;
    private static final int TEXT_DIM = 0xFFAAA0B6;
    private static final int TEXT_OK = 0xFF64E88B;
    private static final int TEXT_WARN = 0xFFFF6868;
    private static final int CYAN = 0xFF6EF5E3;
    private static final int PANEL_WIDTH = 620;
    private static final int PANEL_HEIGHT = 330;
    private static final int SIDEBAR_WIDTH = 132;
    private static final int MARGIN = 8;
    private static final int TAB_HEIGHT = 28;
    private static final int TAB_GAP = 8;
    private static final LedgerTab[] TABS = LedgerTab.values();
    private static final String[] KNOWN_RECIPES = {
        "玻璃瓶 + 紫水晶碎片 + 甜浆果 -> 安神药",
        "纸 + 骨粉 + 灵魂沙 -> 死神残页",
        "墨囊 + 灵魂土 + 荧石粉 -> 收魂墨水"
    };
    private static final String[] BASIC_YARD_RECIPES = {
        "蜂蜜瓶 + 纸 + 紫水晶碎片 -> 褪色邀请函",
        "无名花 + 玻璃瓶 + 月露叶 -> 雾露",
        "褪色邀请函 + 雾露 + 月露叶 -> 记忆碎片",
        "纸 + 线 + 记忆碎片 -> 旧剧票"
    };
    private static final String[] DECORATION_RECIPES = {
        "雾露 + 火把 + 紫水晶碎片 -> 雾灯",
        "纸 + 旧剧票 + 神谕墨水 -> 旧海报"
    };

    private final boolean inYard;
    private final int san;
    private final int unlockedOracleCount;
    private final boolean firstFullScytheReleaseSeen;
    private final int totalSoulsReleased;
    private final int comfort;
    private final int otherworld;
    private final int memory;
    private final String stageName;
    private final String stageDescription;
    private final String stageHint;
    private final boolean hasMistLetter;
    private final int mistLettersAnswered;
    private final boolean hasMistVisitor;
    private final int mistVisitorsHelped;
    private final String mistVisitorTitle;
    private final String mistVisitorRecord;
    private final String mistVisitorRequestText;
    private final String mistVisitorRewardText;
    private final int soulContainerCount;
    private final int storedSouls;
    private final int maxSoulCapacity;
    private final int[] scrollOffsets = new int[TABS.length];
    private LedgerTab selectedTab = LedgerTab.STATUS;

    public YardLedgerScreen(
        boolean inYard,
        int san,
        int unlockedOracleCount,
        boolean firstFullScytheReleaseSeen,
        int totalSoulsReleased,
        int comfort,
        int otherworld,
        int memory,
        String stageName,
        String stageDescription,
        String stageHint,
        boolean hasMistLetter,
        int mistLettersAnswered,
        boolean hasMistVisitor,
        int mistVisitorsHelped,
        String mistVisitorTitle,
        String mistVisitorRecord,
        String mistVisitorRequestText,
        String mistVisitorRewardText,
        int soulContainerCount,
        int storedSouls,
        int maxSoulCapacity
    ) {
        super(Component.literal("特莉波卡的庭院账簿"));
        this.inYard = inYard;
        this.san = san;
        this.unlockedOracleCount = unlockedOracleCount;
        this.firstFullScytheReleaseSeen = firstFullScytheReleaseSeen;
        this.totalSoulsReleased = totalSoulsReleased;
        this.comfort = comfort;
        this.otherworld = otherworld;
        this.memory = memory;
        this.stageName = stageName;
        this.stageDescription = stageDescription;
        this.stageHint = stageHint;
        this.hasMistLetter = hasMistLetter;
        this.mistLettersAnswered = mistLettersAnswered;
        this.hasMistVisitor = hasMistVisitor;
        this.mistVisitorsHelped = mistVisitorsHelped;
        this.mistVisitorTitle = mistVisitorTitle;
        this.mistVisitorRecord = mistVisitorRecord;
        this.mistVisitorRequestText = mistVisitorRequestText;
        this.mistVisitorRewardText = mistVisitorRewardText;
        this.soulContainerCount = soulContainerCount;
        this.storedSouls = storedSouls;
        this.maxSoulCapacity = maxSoulCapacity;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Layout layout = layout();
        guiGraphics.fill(0, 0, this.width, this.height, OVERLAY);
        renderFrame(guiGraphics, layout);
        renderTabs(guiGraphics, layout, mouseX, mouseY);
        renderPage(guiGraphics, layout);
        renderScrollHint(guiGraphics, layout);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0) {
            Layout layout = layout();
            for (int index = 0; index < TABS.length; index++) {
                int tabY = tabY(layout, index);
                if (isInside(mouseX, mouseY, layout.left + 12, tabY, SIDEBAR_WIDTH - 24, TAB_HEIGHT)) {
                    selectedTab = TABS[index];
                    clampSelectedScroll(layout);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        Layout layout = layout();
        if (!isInside(mouseX, mouseY, layout.pageLeft, layout.contentTop, layout.pageWidth, layout.pageHeight())) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        int maxScroll = maxScrollFor(selectedTab, layout);
        if (maxScroll <= 0) {
            return true;
        }

        int index = selectedTab.ordinal();
        scrollOffsets[index] = clamp(scrollOffsets[index] - (int) Math.round(scrollY * 22.0D), 0, maxScroll);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderFrame(GuiGraphics guiGraphics, Layout layout) {
        guiGraphics.fill(layout.left, layout.top, layout.left + layout.width, layout.top + layout.height, BORDER);
        guiGraphics.fill(layout.left + 2, layout.top + 2, layout.left + layout.width - 2, layout.top + layout.height - 2, 0xFF1A0E29);
        guiGraphics.fill(layout.left + 6, layout.top + 6, layout.left + layout.width - 6, layout.top + layout.height - 6, BACKGROUND);
        drawCornerMarks(guiGraphics, layout.left + 8, layout.top + 8, layout.width - 16, layout.height - 16);
        drawStarNoise(guiGraphics, layout.left + 18, layout.top + 18, layout.width - 36, layout.height - 36);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, layout.top + 18, PURPLE);
        guiGraphics.fill(this.width / 2 - 68, layout.top + 42, this.width / 2 + 68, layout.top + 43, PURPLE_DARK);
        guiGraphics.drawCenteredString(this.font, "◇◆◇", this.width / 2, layout.top + 38, PURPLE_DARK);
        guiGraphics.drawString(this.font, "Esc 关闭", layout.left + layout.width - 72, layout.top + 18, TEXT_DIM, true);
        guiGraphics.fill(layout.left + 12, layout.contentTop, layout.left + SIDEBAR_WIDTH, layout.top + layout.height - 12, PANEL_DARK);
        guiGraphics.fill(layout.pageLeft, layout.contentTop, layout.left + layout.width - 12, layout.top + layout.height - 12, PANEL);
    }

    private void renderTabs(GuiGraphics guiGraphics, Layout layout, int mouseX, int mouseY) {
        int x = layout.left + 18;
        for (int index = 0; index < TABS.length; index++) {
            LedgerTab tab = TABS[index];
            int y = tabY(layout, index);
            boolean active = tab == selectedTab;
            boolean hover = isInside(mouseX, mouseY, layout.left + 12, y, SIDEBAR_WIDTH - 24, TAB_HEIGHT);
            int bg = active ? 0xAA3B1C5C : hover ? 0x663B1C5C : 0x00000000;
            guiGraphics.fill(layout.left + 12, y, layout.left + SIDEBAR_WIDTH - 12, y + TAB_HEIGHT, bg);
            if (active) {
                guiGraphics.fill(layout.left + 12, y, layout.left + SIDEBAR_WIDTH - 12, y + 1, PURPLE_DARK);
                guiGraphics.fill(layout.left + 12, y + TAB_HEIGHT - 1, layout.left + SIDEBAR_WIDTH - 12, y + TAB_HEIGHT, PURPLE_DARK);
            }
            guiGraphics.renderFakeItem(tab.icon, x, y + 6);
            guiGraphics.drawString(this.font, tab.label, x + 24, y + 10, active ? TEXT : TEXT_DIM, true);
        }
    }

    private void renderPage(GuiGraphics guiGraphics, Layout layout) {
        int x = layout.pageLeft + 16;
        int y = layout.contentTop + 14;
        int width = layout.pageWidth - 32;
        guiGraphics.drawString(this.font, "◆ " + selectedTab.title, x, y, PURPLE, true);
        guiGraphics.fill(x, y + 15, x + width, y + 16, BORDER_SOFT);
        int contentTop = y + 26;
        int contentBottom = layout.top + layout.height - 18;
        int scroll = scrollOffsets[selectedTab.ordinal()];
        guiGraphics.enableScissor(layout.pageLeft, contentTop - 2, layout.left + layout.width - 18, contentBottom);
        switch (selectedTab) {
            case STATUS -> renderStatusPage(guiGraphics, x, contentTop - scroll, width);
            case LETTERS -> renderLettersPage(guiGraphics, x, contentTop - scroll, width);
            case VISITORS -> renderVisitorsPage(guiGraphics, x, contentTop - scroll, width);
            case SOULS -> renderSoulsPage(guiGraphics, x, contentTop - scroll, width);
            case ALCHEMY -> renderAlchemyPage(guiGraphics, x, contentTop - scroll, width);
        }
        guiGraphics.disableScissor();
    }

    private void renderStatusPage(GuiGraphics guiGraphics, int x, int y, int width) {
        int leftWidth = 226;
        int rightX = x + leftWidth + 12;
        int rightWidth = width - leftWidth - 12;

        drawCard(guiGraphics, x, y, leftWidth, 112);
        guiGraphics.drawString(this.font, "基础状态", x + 16, y + 14, PURPLE, true);
        drawKeyValue(guiGraphics, x + 16, y + 38, leftWidth - 32, "庭院连接", inYard ? "已连接" : "未连接", inYard ? TEXT_OK : TEXT_WARN);
        drawKeyValue(guiGraphics, x + 16, y + 58, leftWidth - 32, "SAN", san + "/100", san <= 30 ? TEXT_WARN : TEXT);
        drawKeyValue(guiGraphics, x + 16, y + 78, leftWidth - 32, "已解锁神谕", unlockedOracleCount + "/6", TEXT);
        drawKeyValue(guiGraphics, x + 16, y + 98, leftWidth - 32, "特莉波卡状态", inYard ? "投影稳定" : "信号微弱", inYard ? TEXT_OK : TEXT_WARN);

        drawCard(guiGraphics, rightX, y, rightWidth, 112);
        guiGraphics.drawString(this.font, "庭院阶段", rightX + 16, y + 14, PURPLE, true);
        guiGraphics.drawString(this.font, fitText(stageName, rightWidth - 32), rightX + 16, y + 40, CYAN, true);
        guiGraphics.drawString(this.font, fitText(stageDescription, rightWidth - 32), rightX + 16, y + 62, TEXT, true);
        guiGraphics.drawString(this.font, fitText(stageHint, rightWidth - 32), rightX + 16, y + 84, TEXT_DIM, true);

        int atmosphereY = y + 128;
        drawCard(guiGraphics, x, atmosphereY, width, 112);
        guiGraphics.drawString(this.font, "庭院氛围", x + 16, atmosphereY + 14, PURPLE, true);
        drawProgress(guiGraphics, x + 24, atmosphereY + 38, width - 48, Math.min(10, comfort), 10, "舒适度", Integer.toString(comfort));
        drawProgress(guiGraphics, x + 24, atmosphereY + 62, width - 48, Math.min(10, otherworld), 10, "异界度", Integer.toString(otherworld));
        drawProgress(guiGraphics, x + 24, atmosphereY + 86, width - 48, Math.min(10, memory), 10, "记忆度", Integer.toString(memory));
    }

    private void renderLettersPage(GuiGraphics guiGraphics, int x, int y, int width) {
        drawCard(guiGraphics, x, y, width, 108);
        guiGraphics.drawString(this.font, "雾夜来信", x + 16, y + 14, PURPLE, true);
        if (hasMistLetter) {
            guiGraphics.drawString(this.font, fitText("“这里有灯……那我应该还没有走错太远。”", width - 34), x + 16, y + 36, TEXT, true);
            drawKeyValue(guiGraphics, x + 16, y + 60, width - 32, "请求", "蜂蜜瓶 x2", TEXT_DIM);
            drawKeyValue(guiGraphics, x + 16, y + 80, width - 32, "回礼", "记忆碎片 x1", TEXT_OK);
        } else {
            guiGraphics.drawString(this.font, "今天没有新的信。", x + 16, y + 42, TEXT_DIM, true);
            guiGraphics.drawString(this.font, "也可能是雾还没有走到门口。", x + 16, y + 62, TEXT, true);
        }

        drawCard(guiGraphics, x, y + 122, width, 70);
        guiGraphics.drawString(this.font, "来信记录", x + 16, y + 136, PURPLE, true);
        guiGraphics.drawString(this.font, "已回应来信：" + mistLettersAnswered + " 封", x + 16, y + 158, TEXT, true);
        guiGraphics.drawString(this.font, "最近一封：信纸变轻了一点。", x + 16, y + 174, TEXT_DIM, true);
    }

    private void renderVisitorsPage(GuiGraphics guiGraphics, int x, int y, int width) {
        drawCard(guiGraphics, x, y, width, 126);
        guiGraphics.drawString(this.font, hasMistVisitor ? mistVisitorTitle : "雾夜来客", x + 16, y + 14, PURPLE, true);
        if (hasMistVisitor) {
            guiGraphics.drawString(this.font, fitText("门外有人停了一会儿。没有敲门。", width - 34), x + 16, y + 36, TEXT, true);
            guiGraphics.drawString(this.font, fitText("特莉波卡记录：" + mistVisitorRecord, width - 34), x + 16, y + 56, TEXT_DIM, true);
            drawKeyValue(guiGraphics, x + 16, y + 80, width - 32, "请求", mistVisitorRequestText, TEXT_DIM);
            drawKeyValue(guiGraphics, x + 16, y + 100, width - 32, "回礼", mistVisitorRewardText, TEXT_OK);
        } else {
            guiGraphics.drawString(this.font, "今晚门口很安静。", x + 16, y + 48, TEXT_DIM, true);
            guiGraphics.drawString(this.font, "也许庭院还不像一个能停留的地方。", x + 16, y + 68, TEXT, true);
        }

        drawCard(guiGraphics, x, y + 144, width, 72);
        guiGraphics.drawString(this.font, "来客记录", x + 16, y + 158, PURPLE, true);
        guiGraphics.drawString(this.font, "已回应来客：" + mistVisitorsHelped + " 位", x + 16, y + 180, TEXT, true);
        guiGraphics.drawString(this.font, "最近一位：留下了一张旧剧票。", x + 16, y + 196, TEXT_DIM, true);
    }

    private void renderSoulsPage(GuiGraphics guiGraphics, int x, int y, int width) {
        drawCard(guiGraphics, x, y, width, 108);
        guiGraphics.drawString(this.font, "收魂容器", x + 16, y + 14, PURPLE, true);
        if (soulContainerCount <= 0) {
            guiGraphics.drawString(this.font, "庭院里还没有能记住名字的容器。", x + 16, y + 48, TEXT_DIM, true);
        } else {
            drawKeyValue(guiGraphics, x + 16, y + 38, width - 32, "容器数量", Integer.toString(soulContainerCount), TEXT);
            drawKeyValue(guiGraphics, x + 16, y + 58, width - 32, "已收容", storedSouls + " / " + maxSoulCapacity, TEXT_OK);
            drawKeyValue(guiGraphics, x + 16, y + 78, width - 32, "可提取收据", (storedSouls / 3) + " 张", TEXT);
            drawKeyValue(guiGraphics, x + 16, y + 94, width - 32, "状态", soulContainerFlavor(), soulContainerFlavorColor());
            drawBar(guiGraphics, x + width - 182, y + 58, 140, 8, storedSouls, Math.max(1, maxSoulCapacity));
        }

        drawCard(guiGraphics, x, y + 124, width, 82);
        guiGraphics.drawString(this.font, "收割记录", x + 16, y + 138, PURPLE, true);
        guiGraphics.drawString(this.font, "累计已处理：" + totalSoulsReleased + " 个", x + 16, y + 160, TEXT, true);
        guiGraphics.drawString(this.font, firstFullScytheReleaseSeen ? "……对不起。" : "残镰还没有满载释放记录。", x + 16, y + 180, firstFullScytheReleaseSeen ? TEXT : TEXT_DIM, true);
    }

    private void renderAlchemyPage(GuiGraphics guiGraphics, int x, int y, int width) {
        int cardHeight = 42;
        int currentY = y;
        currentY = drawRecipeCards(guiGraphics, "已有配方", KNOWN_RECIPES, x, currentY, width, cardHeight);
        currentY = drawRecipeCards(guiGraphics, "基础庭院素材", BASIC_YARD_RECIPES, x, currentY + 8, width, cardHeight);
        drawRecipeCards(guiGraphics, "庭院装饰", DECORATION_RECIPES, x, currentY + 8, width, cardHeight);
    }

    private void renderScrollHint(GuiGraphics guiGraphics, Layout layout) {
        int maxScroll = maxScrollFor(selectedTab, layout);
        if (maxScroll <= 0) {
            return;
        }

        int barX = layout.left + layout.width - 18;
        int barTop = layout.contentTop + 42;
        int barHeight = layout.pageHeight() - 58;
        int thumbHeight = Math.max(22, barHeight * layout.visibleContentHeight() / contentHeightFor(selectedTab));
        int thumbY = barTop + (barHeight - thumbHeight) * scrollOffsets[selectedTab.ordinal()] / maxScroll;
        guiGraphics.fill(barX, barTop, barX + 3, barTop + barHeight, 0x552A1A40);
        guiGraphics.fill(barX, thumbY, barX + 3, thumbY + thumbHeight, PURPLE_DARK);
    }

    private void drawCard(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, BORDER_SOFT);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_SOFT);
        guiGraphics.fill(x + 4, y + 4, x + width - 4, y + height - 4, 0x5510081B);
    }

    private void drawProgress(GuiGraphics guiGraphics, int x, int y, int width, int value, int max, String label, String valueText) {
        guiGraphics.drawString(this.font, label + "：", x, y, TEXT_DIM, true);
        guiGraphics.drawString(this.font, valueText, x + width - this.font.width(valueText), y, PURPLE, true);
        drawBar(guiGraphics, x, y + 13, width, 7, value, max);
    }

    private void drawBar(GuiGraphics guiGraphics, int x, int y, int width, int height, int value, int max) {
        int fill = max <= 0 ? 0 : width * clamp(value, 0, max) / max;
        guiGraphics.fill(x, y, x + width, y + height, 0xAA2A183D);
        guiGraphics.fill(x, y, x + fill, y + height, 0xFF8E35FF);
        guiGraphics.fill(x, y, x + fill, y + 2, 0xFFCA88FF);
        guiGraphics.fill(x, y + height, x + width, y + height + 1, BORDER_SOFT);
    }

    private int drawRecipeCards(GuiGraphics guiGraphics, String title, String[] recipes, int x, int y, int width, int cardHeight) {
        guiGraphics.drawString(this.font, title, x, y, PURPLE, true);
        int currentY = y + 16;
        for (String recipe : recipes) {
            drawCard(guiGraphics, x, currentY, width, cardHeight);
            guiGraphics.drawString(this.font, fitText(recipe, width - 28), x + 14, currentY + 16, TEXT, true);
            currentY += cardHeight + 6;
        }
        return currentY;
    }

    private void drawKeyValue(GuiGraphics guiGraphics, int x, int y, int width, String key, String value, int valueColor) {
        int valueX = x + Math.min(92, Math.max(58, width / 2));
        guiGraphics.drawString(this.font, key + "：", x, y, TEXT_DIM, true);
        guiGraphics.drawString(this.font, fitText(value, x + width - valueX), valueX, y, valueColor, true);
    }

    private void drawCornerMarks(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int size = 10;
        guiGraphics.fill(x, y, x + size, y + 1, BORDER);
        guiGraphics.fill(x, y, x + 1, y + size, BORDER);
        guiGraphics.fill(x + width - size, y, x + width, y + 1, BORDER);
        guiGraphics.fill(x + width - 1, y, x + width, y + size, BORDER);
        guiGraphics.fill(x, y + height - 1, x + size, y + height, BORDER);
        guiGraphics.fill(x, y + height - size, x + 1, y + height, BORDER);
        guiGraphics.fill(x + width - size, y + height - 1, x + width, y + height, BORDER);
        guiGraphics.fill(x + width - 1, y + height - size, x + width, y + height, BORDER);
    }

    private void drawStarNoise(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        for (int i = 0; i < 42; i++) {
            int px = x + Math.floorMod(i * 47, Math.max(1, width));
            int py = y + Math.floorMod(i * 29, Math.max(1, height));
            int color = i % 4 == 0 ? 0x338E4FFF : 0x223A2458;
            guiGraphics.fill(px, py, px + 1, py + 1, color);
        }
    }

    private String soulContainerFlavor() {
        if (storedSouls <= 0) {
            return "空的。很安静。";
        }
        if (storedSouls >= maxSoulCapacity) {
            return "太满了。会漏出来。";
        }
        if (storedSouls >= maxSoulCapacity / 2) {
            return "声音变多了。";
        }
        return "有些名字还在里面。";
    }

    private int soulContainerFlavorColor() {
        if (storedSouls >= maxSoulCapacity) {
            return TEXT_WARN;
        }
        if (storedSouls > 0) {
            return TEXT_OK;
        }
        return TEXT_DIM;
    }

    private String fitText(String text, int maxWidth) {
        if (text == null || text.isEmpty() || this.font.width(text) <= maxWidth) {
            return text == null ? "" : text;
        }

        String ellipsis = "...";
        int end = text.length();
        while (end > 0 && this.font.width(text.substring(0, end)) + this.font.width(ellipsis) > maxWidth) {
            end--;
        }
        return text.substring(0, Math.max(0, end)) + ellipsis;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private void clampSelectedScroll(Layout layout) {
        int index = selectedTab.ordinal();
        scrollOffsets[index] = clamp(scrollOffsets[index], 0, maxScrollFor(selectedTab, layout));
    }

    private int maxScrollFor(LedgerTab tab, Layout layout) {
        return Math.max(0, contentHeightFor(tab) - layout.visibleContentHeight());
    }

    private int contentHeightFor(LedgerTab tab) {
        return switch (tab) {
            case STATUS -> 256;
            case LETTERS -> 204;
            case VISITORS -> 228;
            case SOULS -> 220;
            case ALCHEMY -> 510;
        };
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private Layout layout() {
        int panelWidth = Math.min(PANEL_WIDTH, this.width - MARGIN * 2);
        int panelHeight = Math.min(PANEL_HEIGHT, this.height - MARGIN * 2);
        int left = Math.max(MARGIN, (this.width - panelWidth) / 2);
        int top = Math.max(MARGIN, (this.height - panelHeight) / 2);
        int pageLeft = left + SIDEBAR_WIDTH + 12;
        int contentTop = top + 54;
        return new Layout(left, top, panelWidth, panelHeight, pageLeft, contentTop, left + panelWidth - pageLeft - 12);
    }

    private int tabY(Layout layout, int index) {
        return layout.contentTop + 14 + index * (TAB_HEIGHT + TAB_GAP);
    }

    private enum LedgerTab {
        STATUS("庭院状态", "庭院状态", new ItemStack(Items.AMETHYST_CLUSTER)),
        LETTERS("雾夜来信", "雾夜来信", new ItemStack(Items.PAPER)),
        VISITORS("雾夜来客", "雾夜来客", new ItemStack(Items.ENDER_EYE)),
        SOULS("收割记录", "收割记录", new ItemStack(Items.SOUL_LANTERN)),
        ALCHEMY("炼金配方", "庭院炼金", new ItemStack(Items.POTION));

        private final String label;
        private final String title;
        private final ItemStack icon;

        LedgerTab(String label, String title, ItemStack icon) {
            this.label = label;
            this.title = title;
            this.icon = icon;
        }
    }

    private record Layout(int left, int top, int width, int height, int pageLeft, int contentTop, int pageWidth) {
        private int pageHeight() {
            return top + height - 12 - contentTop;
        }

        private int visibleContentHeight() {
            return pageHeight() - 54;
        }
    }
}
