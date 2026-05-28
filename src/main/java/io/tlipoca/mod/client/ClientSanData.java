package io.tlipoca.mod.client;

public final class ClientSanData {
    private static int localSan = 100;
    private static boolean forbiddenActive;

    private ClientSanData() {
    }

    public static int getLocalSan() {
        return localSan;
    }

    public static void setLocalSan(int san) {
        localSan = san;
    }

    public static boolean isForbiddenActive() {
        return forbiddenActive;
    }

    public static void setForbiddenActive(boolean active) {
        forbiddenActive = active;
    }
}
