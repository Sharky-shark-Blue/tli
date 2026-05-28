package io.tlipoca.mod.client;

import io.tlipoca.mod.network.OpenOracleScreenMessage;
import net.minecraft.client.Minecraft;

public final class OracleScreenOpener {
    private OracleScreenOpener() {
    }

    public static void open(OpenOracleScreenMessage message) {
        Minecraft.getInstance().setScreen(new OracleScreen(message.stars()));
    }
}
