package io.tlipoca.mod.network;

import net.minecraft.network.FriendlyByteBuf;

public record OpenOracleScreenMessage(int[] stars) {
    public static void encode(OpenOracleScreenMessage message, FriendlyByteBuf buffer) {
        for (int star : message.stars()) {
            buffer.writeVarInt(star);
        }
    }

    public static OpenOracleScreenMessage decode(FriendlyByteBuf buffer) {
        int[] stars = new int[6];
        for (int index = 0; index < stars.length; index++) {
            stars[index] = buffer.readVarInt();
        }
        return new OpenOracleScreenMessage(stars);
    }
}
