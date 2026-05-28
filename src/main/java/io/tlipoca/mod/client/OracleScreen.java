package io.tlipoca.mod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.List;

public class OracleScreen extends Screen {
    private static final int BACKGROUND = 0xEE1A0A2E;
    private static final int BORDER_OUTER = 0xFF4A3060;
    private static final int BORDER_INNER = 0xFF2A1A40;
    private static final int LIST_BG = 0xFF151018;
    private static final int DETAIL_BG = 0xFF1D1428;
    private static final int ROW_UNLOCKED = 0xFF2A1A40;
    private static final int ROW_LOCKED = 0xFF1A1A1A;
    private static final int ROW_SELECTED = 0xFF3A2458;
    private static final int BORDER_UNLOCKED = 0xFF7F77DD;
    private static final int BORDER_LOCKED = 0xFF333333;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_GRAY = 0xFFB8B8B8;
    private static final int TEXT_LOCKED = 0xFF555555;
    private static final int TEXT_COST = 0xFFFF5555;
    private static final int TEXT_INFO = 0xFF88DDFF;
    private static final int TEXT_NEXT = 0xFFFFDD88;
    private static final int TITLE_GOLD = 0xFFFFAA00;
    private static final int PANEL_WIDTH = 520;
    private static final int PANEL_HEIGHT = 258;
    private static final int LIST_WIDTH = 178;
    private static final int ROW_HEIGHT = 28;
    private static final int ROW_GAP = 4;
    private static final int PADDING = 12;

    private static final List<OracleCard> CARDS = List.of(
            new OracleCard("达贡的黄金", "达贡", 0xFF4488FF, "触发：水中挖掘", "效果：获得急迫II加速挖掘", "代价：离水超5分钟饱食度缓慢流失", Items.GOLD_INGOT.getDefaultInstance(), 0xFF4488FF, 0),
            new OracleCard("地脉记忆", "地之古神", 0xFFAA7744, "触发：挖掘矿石（概率）", "效果：附近同类矿石发出光柱指引", "代价：每次触发SAN-1", Blocks.DIAMOND_ORE.asItem().getDefaultInstance(), 0xFFAA7744, 12),
            new OracleCard("猎者凝视", "猎者", 0xFFFF4444, "触发：击杀敌对生物（概率）", "效果：攻击力+2持续8秒可叠加3层", "代价：每次触发SAN-1", Items.BOW.getDefaultInstance(), 0xFFFF4444, 8),
            new OracleCard("禁忌活力", "禁忌之源", 0xFFAA00AA, "触发：SAN降至30以下自动激活", "效果：获得力量I，SAN≤15额外获得抗性提升", "代价：以理智换取力量", Items.FERMENTED_SPIDER_EYE.getDefaultInstance(), 0xFFAA00AA, 0),
            new OracleCard("雾中步伐", "雾之行者", 0xFF88DDDD, "触发：夜晚露天持续生效", "效果：移动速度提升，周围出现幽魂粒子", "代价：无", Items.FEATHER.getDefaultInstance(), 0xFF88DDDD, 0),
            new OracleCard("深渊回响", "深渊", 0xFF00CC88, "触发：每次占卜后概率激活", "效果：下次受到伤害完全抵消", "代价：护盾触发时SAN-3", Items.ENDER_EYE.getDefaultInstance(), 0xFF00CC88, 8)
    );

    private final int[] stars;
    private int selectedIndex;

    public OracleScreen(int[] stars) {
        super(Component.literal("神谕典藏"));
        this.stars = Arrays.copyOf(stars, CARDS.size());
        this.selectedIndex = firstUnlockedIndex();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int left = Math.max(4, (this.width - PANEL_WIDTH) / 2);
        int top = Math.max(4, (this.height - PANEL_HEIGHT) / 2);

        renderPanel(guiGraphics, left, top);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, top + 12, TITLE_GOLD);

        int contentTop = top + 34;
        int listLeft = left + PADDING;
        int detailLeft = listLeft + LIST_WIDTH + 10;
        int detailWidth = PANEL_WIDTH - PADDING * 2 - LIST_WIDTH - 10;
        int contentHeight = PANEL_HEIGHT - 62;

        renderOracleList(guiGraphics, listLeft, contentTop);
        renderOracleDetail(guiGraphics, detailLeft, contentTop, detailWidth, contentHeight);
        renderStatusBar(guiGraphics, left, top);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int left = Math.max(4, (this.width - PANEL_WIDTH) / 2);
        int top = Math.max(4, (this.height - PANEL_HEIGHT) / 2);
        int listLeft = left + PADDING;
        int listTop = top + 34;

        for (int index = 0; index < CARDS.size(); index++) {
            int rowY = listTop + index * (ROW_HEIGHT + ROW_GAP);
            if (mouseX >= listLeft && mouseX < listLeft + LIST_WIDTH && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                selectedIndex = index;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderPanel(GuiGraphics guiGraphics, int left, int top) {
        guiGraphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, BORDER_OUTER);
        guiGraphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + PANEL_HEIGHT - 2, BORDER_INNER);
        guiGraphics.fill(left + 5, top + 5, left + PANEL_WIDTH - 5, top + PANEL_HEIGHT - 5, BACKGROUND);
    }

    private void renderOracleList(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 4, y - 4, x + LIST_WIDTH + 4, y + 6 * (ROW_HEIGHT + ROW_GAP), LIST_BG);
        for (int index = 0; index < CARDS.size(); index++) {
            renderOracleRow(guiGraphics, CARDS.get(index), x, y + index * (ROW_HEIGHT + ROW_GAP), stars[index], index == selectedIndex);
        }
    }

    private void renderOracleRow(GuiGraphics guiGraphics, OracleCard card, int x, int y, int star, boolean selected) {
        boolean unlocked = star > 0;
        int border = selected ? BORDER_UNLOCKED : (unlocked ? BORDER_UNLOCKED : BORDER_LOCKED);
        int background = selected ? ROW_SELECTED : (unlocked ? ROW_UNLOCKED : ROW_LOCKED);
        int textColor = unlocked ? TEXT_WHITE : TEXT_LOCKED;

        guiGraphics.fill(x, y, x + LIST_WIDTH, y + ROW_HEIGHT, border);
        guiGraphics.fill(x + 1, y + 1, x + LIST_WIDTH - 1, y + ROW_HEIGHT - 1, background);
        guiGraphics.fill(x + 3, y + 3, x + 6, y + ROW_HEIGHT - 3, unlocked ? card.stripColor() : BORDER_LOCKED);
        guiGraphics.renderFakeItem(card.icon(), x + 10, y + 6);
        if (!unlocked) {
            guiGraphics.fill(x + 10, y + 6, x + 26, y + 22, 0xAA444444);
        }

        guiGraphics.drawString(this.font, fitText(card.name(), 94), x + 34, y + 5, textColor, true);
        guiGraphics.drawString(this.font, starText(star), x + LIST_WIDTH - 24, y + 5, starColor(star), true);
        guiGraphics.drawString(this.font, unlocked ? "归属：" + card.patron() : "未解锁", x + 34, y + 17, unlocked ? card.patronColor() : TEXT_LOCKED, true);
    }

    private void renderOracleDetail(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        OracleCard card = CARDS.get(selectedIndex);
        int star = stars[selectedIndex];
        boolean unlocked = star > 0;
        int textColor = unlocked ? TEXT_WHITE : TEXT_LOCKED;
        int secondaryColor = unlocked ? TEXT_GRAY : TEXT_LOCKED;

        guiGraphics.fill(x, y, x + width, y + height, unlocked ? BORDER_UNLOCKED : BORDER_LOCKED);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, DETAIL_BG);
        guiGraphics.fill(x + 8, y + 12, x + 36, y + 40, 0xFF0E0A14);
        guiGraphics.renderFakeItem(card.icon(), x + 14, y + 18);
        if (!unlocked) {
            guiGraphics.fill(x + 8, y + 12, x + 36, y + 40, 0xAA444444);
        }

        guiGraphics.drawString(this.font, card.name(), x + 46, y + 13, textColor, true);
        guiGraphics.drawString(this.font, unlocked ? starText(star) : "✦", x + width - 28, y + 13, starColor(star), true);
        guiGraphics.drawString(this.font, unlocked ? "归属：" + card.patron() : "未解锁", x + 46, y + 28, unlocked ? card.patronColor() : TEXT_LOCKED, true);

        int lineX = x + 16;
        int lineWidth = width - 32;
        guiGraphics.drawString(this.font, fitText(card.triggerLine(), lineWidth), lineX, y + 58, secondaryColor, true);
        guiGraphics.drawString(this.font, fitText(card.effectLine(), lineWidth), lineX, y + 78, secondaryColor, true);
        guiGraphics.drawString(this.font, fitText(card.costLine(), lineWidth), lineX, y + 98, unlocked ? TEXT_COST : TEXT_LOCKED, true);
        guiGraphics.drawString(this.font, fitText(chanceLine(card, star), lineWidth), lineX, y + 122, unlocked ? TEXT_INFO : TEXT_LOCKED, true);
        guiGraphics.drawString(this.font, fitText(nextPreviewLine(card, star), lineWidth), lineX, y + 142, unlocked ? TEXT_NEXT : TEXT_LOCKED, true);
    }

    private void renderStatusBar(GuiGraphics guiGraphics, int left, int top) {
        int unlocked = 0;
        for (int star : stars) {
            if (star > 0) {
                unlocked++;
            }
        }
        int barLeft = left + 8;
        int barRight = left + PANEL_WIDTH - 8;
        int statusY = top + PANEL_HEIGHT - 24;
        guiGraphics.fill(barLeft, statusY - 4, barRight, statusY + 14, 0xAA12091F);

        String rightText = "SAN: " + ClientSanData.getLocalSan() + "/100";
        int rightX = barRight - 10 - this.font.width(rightText);
        guiGraphics.drawString(this.font, "已解锁 " + unlocked + " / 6 个神谕", barLeft + 10, statusY, 0xFFE8D8FF, true);
        guiGraphics.drawString(this.font, rightText, rightX, statusY, 0xFFE8D8FF, true);
    }

    private int firstUnlockedIndex() {
        for (int index = 0; index < stars.length; index++) {
            if (stars[index] > 0) {
                return index;
            }
        }
        return 0;
    }

    private String fitText(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int end = text.length();
        while (end > 0 && this.font.width(text.substring(0, end)) + this.font.width(ellipsis) > maxWidth) {
            end--;
        }
        return text.substring(0, Math.max(0, end)) + ellipsis;
    }

    private static String starText(int star) {
        return switch (star) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "✦";
        };
    }

    private static int starColor(int star) {
        return switch (star) {
            case 1 -> 0xFFAAAAAA;
            case 2 -> 0xFF55FF55;
            case 3 -> 0xFF5555FF;
            case 4 -> 0xFFAA00AA;
            case 5 -> 0xFFFFAA00;
            default -> TEXT_LOCKED;
        };
    }

    private static String chanceLine(OracleCard card, int star) {
        if (star <= 0) {
            return "触发概率：未解锁";
        }
        if (card.chancePerStar() <= 0) {
            return "触发概率：满足条件必定生效";
        }
        return "触发概率：" + (star * card.chancePerStar()) + "%";
    }

    private static String nextPreviewLine(OracleCard card, int star) {
        if (star >= 5) {
            return "下一级预览：已满星";
        }

        int nextStar = Math.max(1, star + 1);
        if (card.chancePerStar() <= 0) {
            return "下一级预览：" + starText(nextStar) + " 星，效果保持稳定";
        }
        return "下一级预览：" + starText(nextStar) + " 星，概率提升至 " + (nextStar * card.chancePerStar()) + "%";
    }

    private record OracleCard(
            String name,
            String patron,
            int patronColor,
            String triggerLine,
            String effectLine,
            String costLine,
            ItemStack icon,
            int stripColor,
            int chancePerStar
    ) {
    }
}
