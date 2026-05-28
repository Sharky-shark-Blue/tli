package io.tlipoca.mod.client;

import io.tlipoca.mod.bounty.BountyDefinition;
import io.tlipoca.mod.bounty.BountyManager;
import io.tlipoca.mod.network.OpenBountyBoardScreenMessage;
import io.tlipoca.mod.network.TlipocaNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class BountyBoardScreen extends Screen {
    private static final int BACKGROUND = 0xEE140A1F;
    private static final int BORDER_OUTER = 0xFF4A3060;
    private static final int BORDER_INNER = 0xFF2A1A40;
    private static final int ROW_BG = 0xAA12091F;
    private static final int TITLE_GOLD = 0xFFFFAA00;
    private static final int TEXT_GRAY = 0xFFD8D0DD;
    private static final int TEXT_DIM = 0xFF9D93AA;
    private static final int TEXT_OK = 0xFF88DDCC;
    private static final int TEXT_WARN = 0xFFFFAA66;
    private static final int PANEL_WIDTH = 500;
    private static final int PANEL_HEIGHT = 236;
    private static final int SCREEN_MARGIN = 8;

    private final OpenBountyBoardScreenMessage message;

    public BountyBoardScreen(OpenBountyBoardScreenMessage message) {
        super(Component.literal("特莉波卡的收魂悬赏"));
        this.message = message;
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
        guiGraphics.drawString(this.font, "刷新剩余：" + BountyManager.formatRemainingTime(message.ticksUntilRefresh()), left + 18, top + 34, TEXT_DIM, true);

        int rowLeft = left + 18;
        int rowWidth = panelWidth - 36;
        for (int slot = 0; slot < BountyManager.SLOT_COUNT; slot++) {
            renderBountyRow(guiGraphics, rowLeft, top + 56 + slot * 54, rowWidth, slot);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int panelWidth = Math.min(PANEL_WIDTH, this.width - SCREEN_MARGIN * 2);
        int panelHeight = Math.min(PANEL_HEIGHT, this.height - SCREEN_MARGIN * 2);
        int left = Math.max(SCREEN_MARGIN, (this.width - panelWidth) / 2);
        int top = Math.max(SCREEN_MARGIN, (this.height - panelHeight) / 2);
        int rowLeft = left + 18;
        int rowWidth = panelWidth - 36;

        for (int slot = 0; slot < BountyManager.SLOT_COUNT; slot++) {
            int rowY = top + 56 + slot * 54;
            if (mouseX >= rowLeft - 6 && mouseX < rowLeft + rowWidth + 6 && mouseY >= rowY - 5 && mouseY < rowY + 45) {
                if (!message.claimed()[slot]) {
                    TlipocaNetwork.claimBounty(message.boardPos(), slot);
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void renderBountyRow(GuiGraphics guiGraphics, int x, int y, int width, int slot) {
        BountyDefinition definition = BountyManager.getDefinition(message.bountyIds()[slot]);
        guiGraphics.fill(x - 6, y - 5, x + width + 6, y + 45, ROW_BG);
        guiGraphics.drawString(this.font, definition.name(), x, y, TITLE_GOLD, true);
        guiGraphics.drawString(this.font, message.claimed()[slot] ? "已领取" : "可领取", x + width - 46, y, message.claimed()[slot] ? TEXT_WARN : TEXT_OK, true);
        guiGraphics.drawString(this.font, fitText("目标：" + definition.targetText(), width), x, y + 14, TEXT_GRAY, true);
        guiGraphics.drawString(this.font, fitText("奖励：" + definition.rewardText(), width), x, y + 28, TEXT_DIM, true);
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
