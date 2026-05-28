package io.tlipoca.mod.client;

import io.tlipoca.mod.TlipocaMod;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public final class SanHudRenderer {
    private static final ResourceLocation SAN_HUD = ResourceLocation.fromNamespaceAndPath(TlipocaMod.MODID, "san_hud");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean renderedOnce;

    private SanHudRenderer() {
    }

    public static void register(AddGuiOverlayLayersEvent event) {
        LOGGER.info("SAN HUD register");
        event.getLayeredDraw().addAbove(
            ForgeLayeredDraw.POST_SLEEP_STACK,
            SAN_HUD,
            ForgeLayeredDraw.CHAT_OVERLAY,
            SanHudRenderer::render
        );
    }

    private static void render(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (!renderedOnce) {
            renderedOnce = true;
            LOGGER.info("SAN HUD render");
        }

        guiGraphics.nextStratum();
        guiGraphics.fill(4, 4, 82, 18, 0x66000000);
        int sanLabelColor = ClientSanData.isForbiddenActive() ? 0xFFFF4444 : 0xFFFFFFFF;
        guiGraphics.drawString(minecraft.font, Component.literal("SAN"), 8, 8, sanLabelColor, true);
        guiGraphics.drawString(minecraft.font, Component.literal(": " + ClientSanData.getLocalSan() + "/100"), 8 + minecraft.font.width("SAN"), 8, 0xFFFFFFFF, true);
    }
}
