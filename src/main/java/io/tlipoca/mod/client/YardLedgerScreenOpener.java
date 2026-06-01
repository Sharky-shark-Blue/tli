package io.tlipoca.mod.client;

import io.tlipoca.mod.network.OpenYardLedgerScreenMessage;
import net.minecraft.client.Minecraft;

public final class YardLedgerScreenOpener {
    private YardLedgerScreenOpener() {
    }

    public static void open(OpenYardLedgerScreenMessage message) {
        Minecraft.getInstance().setScreen(new YardLedgerScreen(
            message.inYard(),
            message.san(),
            message.unlockedOracleCount(),
            message.firstFullScytheReleaseSeen(),
            message.totalSoulsReleased(),
            message.comfort(),
            message.otherworld(),
            message.memory(),
            message.archivedMemoryFragments(),
            message.memoryArchiveStage(),
            message.memoryArchiveText(),
            message.memoryArchiveNextHint(),
            message.stageName(),
            message.stageDescription(),
            message.stageHint(),
            message.hasMistLetter(),
            message.mistLettersAnswered(),
            message.hasMistVisitor(),
            message.mistVisitorsHelped(),
            message.mistVisitorRecordedTypes(),
            message.mistVisitorTotalTypes(),
            message.mistVisitorRecordSummary(),
            message.mistVisitorTitle(),
            message.mistVisitorRecord(),
            message.mistVisitorRequestText(),
            message.mistVisitorRewardText(),
            message.soulContainerCount(),
            message.storedSouls(),
            message.maxSoulCapacity()
        ));
    }
}
