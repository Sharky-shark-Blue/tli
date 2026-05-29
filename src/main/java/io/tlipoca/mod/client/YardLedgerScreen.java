package io.tlipoca.mod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class YardLedgerScreen extends Screen {
    private static final int BACKGROUND = 0xEE140A1F;
    private static final int BORDER_OUTER = 0xFF4A3060;
    private static final int BORDER_INNER = 0xFF2A1A40;
    private static final int SECTION_BG = 0xAA12091F;
    private static final int TITLE_GOLD = 0xFFFFAA00;
    private static final int TEXT_GRAY = 0xFFD8D0DD;
    private static final int TEXT_DIM = 0xFF9D93AA;
    private static final int TEXT_WARN = 0xFFFF6666;
    private static final int TEXT_OK = 0xFF88DDCC;
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 390;
    private static final int SCREEN_MARGIN = 8;
    private static final int RECIPE_LINE_HEIGHT = 10;
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
        boolean hasMistLetter
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
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int panelWidth = Math.min(PANEL_WIDTH, this.width - SCREEN_MARGIN * 2);
        int panelHeight = Math.min(PANEL_HEIGHT, this.height - SCREEN_MARGIN * 2);
        int left = Math.max(SCREEN_MARGIN, (this.width - panelWidth) / 2);
        int top = Math.max(SCREEN_MARGIN, (this.height - panelHeight) / 2);

        guiGraphics.fill(left, top, left + panelWidth, top + panelHeight, BORDER_OUTER);
        guiGraphics.fill(left + 2, top + 2, left + panelWidth - 2, top + panelHeight - 2, BORDER_INNER);
        guiGraphics.fill(left + 5, top + 5, left + panelWidth - 5, top + panelHeight - 5, BACKGROUND);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, top + 14, TITLE_GOLD);

        int contentX = left + 18;
        int contentWidth = panelWidth - 36;
        renderStatus(guiGraphics, contentX, top + 38, contentWidth);
        int recordY = top + 131;
        if (hasMistLetter) {
            renderMistLetter(guiGraphics, contentX, recordY, contentWidth);
            recordY += 66;
        }
        renderRecord(guiGraphics, contentX, recordY, contentWidth);
        int recipesY = recordY + 41;
        if (firstFullScytheReleaseSeen) {
            renderHarvestRecord(guiGraphics, contentX, recipesY, contentWidth);
            recipesY += 48;
        }
        renderRecipes(guiGraphics, contentX, recipesY, contentWidth);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderStatus(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.fill(x - 6, y - 6, x + width + 6, y + 86, SECTION_BG);
        guiGraphics.drawString(this.font, "庭院状态", x, y, TITLE_GOLD, true);
        drawKeyValue(guiGraphics, x, y + 16, "庭院连接", inYard ? "已连接" : "未连接", inYard ? TEXT_OK : TEXT_WARN);
        drawKeyValue(guiGraphics, x, y + 30, "SAN", san + "/100", san <= 30 ? TEXT_WARN : TEXT_GRAY);
        drawKeyValue(guiGraphics, x, y + 44, "已解锁神谕", unlockedOracleCount + "/6", TEXT_GRAY);
        drawKeyValue(guiGraphics, x, y + 58, "特莉波卡状态", inYard ? "投影稳定" : "信号微弱", inYard ? TEXT_OK : TEXT_WARN);

        int atmosphereX = x + Math.max(190, width / 2);
        int leftColumnWidth = atmosphereX - x - 12;
        int rightColumnWidth = width - (atmosphereX - x);
        guiGraphics.drawString(this.font, "庭院氛围", atmosphereX, y, TITLE_GOLD, true);
        drawKeyValue(guiGraphics, atmosphereX, y + 16, "舒适度", Integer.toString(comfort), TEXT_GRAY);
        drawKeyValue(guiGraphics, atmosphereX, y + 30, "异界度", Integer.toString(otherworld), TEXT_GRAY);
        drawKeyValue(guiGraphics, atmosphereX, y + 44, "记忆度", Integer.toString(memory), TEXT_GRAY);
        drawKeyValue(guiGraphics, atmosphereX, y + 58, "庭院阶段", stageName, TEXT_OK);

        guiGraphics.drawString(this.font, fitText(stageDescription, leftColumnWidth), x, y + 72, TEXT_GRAY, true);
        guiGraphics.drawString(this.font, fitText(stageHint, rightColumnWidth), atmosphereX, y + 72, TEXT_DIM, true);
    }

    private void renderRecord(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.fill(x - 6, y - 6, x + width + 6, y + 34, SECTION_BG);
        guiGraphics.drawString(this.font, "特莉波卡记录", x, y, TITLE_GOLD, true);
        drawWrapped(guiGraphics, inYard
            ? "锚点还算稳定。至少这里的东西不会立刻把自己炼成灰。"
            : "离庭院太远了，我的声音听起来是不是有点像坏掉的唱片？",
            x, y + 14, width, TEXT_GRAY, 2);
    }

    private void renderRecipes(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.fill(x - 6, y - 6, x + width + 6, y + 94, SECTION_BG);
        guiGraphics.drawString(this.font, "已知庭院炼金", x, y, TITLE_GOLD, true);

        int columnGap = 14;
        int columnWidth = (width - columnGap) / 2;
        int rightX = x + columnWidth + columnGap;
        drawRecipeGroup(guiGraphics, "已有配方", KNOWN_RECIPES, x, y + 14, columnWidth);
        int nextY = drawRecipeGroup(guiGraphics, "基础庭院素材", BASIC_YARD_RECIPES, rightX, y + 14, columnWidth);
        drawRecipeGroup(guiGraphics, "庭院装饰", DECORATION_RECIPES, rightX, nextY + 2, columnWidth);
    }

    private void renderMistLetter(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.fill(x - 6, y - 6, x + width + 6, y + 58, SECTION_BG);
        guiGraphics.drawString(this.font, "雾夜来信", x, y, TITLE_GOLD, true);
        guiGraphics.drawString(this.font, fitText("“这里有灯……那我应该还没有走错太远。”", width), x, y + 14, TEXT_GRAY, true);
        guiGraphics.drawString(this.font, "请求：蜂蜜瓶 x2", x, y + 30, TEXT_DIM, true);
        guiGraphics.drawString(this.font, "回礼：记忆碎片 x1", x, y + 44, TEXT_OK, true);
    }

    private int drawRecipeGroup(GuiGraphics guiGraphics, String title, String[] recipes, int x, int y, int width) {
        guiGraphics.drawString(this.font, title, x, y, TEXT_DIM, true);
        int lineY = y + RECIPE_LINE_HEIGHT;
        for (String recipe : recipes) {
            guiGraphics.drawString(this.font, fitText(recipe, width), x, lineY, TEXT_GRAY, true);
            lineY += RECIPE_LINE_HEIGHT;
        }
        return lineY;
    }

    private void renderHarvestRecord(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.fill(x - 6, y - 6, x + width + 6, y + 40, SECTION_BG);
        guiGraphics.drawString(this.font, "收割记录", x, y, TITLE_GOLD, true);
        guiGraphics.drawString(this.font, "累计已处理：" + totalSoulsReleased + " 个", x, y + 14, TEXT_GRAY, true);
        guiGraphics.drawString(this.font, "……对不起。", x, y + 28, TEXT_GRAY, true);
    }

    private void drawKeyValue(GuiGraphics guiGraphics, int x, int y, String key, String value, int valueColor) {
        guiGraphics.drawString(this.font, key + "：", x, y, TEXT_DIM, true);
        guiGraphics.drawString(this.font, value, x + 92, y, valueColor, true);
    }

    private void drawWrapped(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color, int maxLines) {
        int line = 0;
        String remaining = text;
        while (!remaining.isEmpty() && line < maxLines) {
            String fitted = fitText(remaining, maxWidth);
            if (line == maxLines - 1 || fitted.length() >= remaining.length()) {
                guiGraphics.drawString(this.font, fitted, x, y + line * 12, color, true);
                return;
            }
            guiGraphics.drawString(this.font, fitted, x, y + line * 12, color, true);
            remaining = remaining.substring(fitted.length()).stripLeading();
            line++;
        }
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
}
