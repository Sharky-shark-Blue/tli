package io.tlipoca.mod.client;

import io.tlipoca.mod.network.OpenBountyBoardScreenMessage;
import net.minecraft.client.Minecraft;

public final class BountyBoardScreenOpener {
    private BountyBoardScreenOpener() {
    }

    public static void open(OpenBountyBoardScreenMessage message) {
        Minecraft.getInstance().setScreen(new BountyBoardScreen(message));
    }
}
