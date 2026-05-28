package io.tlipoca.mod.client;

import io.tlipoca.mod.network.SanSyncMessage;

public final class ClientSanSyncHandler {
    private ClientSanSyncHandler() {
    }

    public static void handle(SanSyncMessage message) {
        ClientSanData.setLocalSan(message.san());
        ClientSanData.setForbiddenActive(message.forbiddenActive());
    }
}
