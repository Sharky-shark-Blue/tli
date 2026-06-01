package io.tlipoca.mod.network;

import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.block.entity.BountyBoardBlockEntity;
import io.tlipoca.mod.bounty.BountyManager;
import io.tlipoca.mod.oracle.OracleManager;
import io.tlipoca.mod.oracle.PlayerOracleData;
import io.tlipoca.mod.yard.MistVisitorRequest;
import io.tlipoca.mod.yard.YardManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class TlipocaNetwork {
    private static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(TlipocaMod.MODID, "main"))
            .networkProtocolVersion(1)
            .simpleChannel();

    private TlipocaNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(SanSyncMessage.class, 0)
                .direction(NetworkDirection.PLAY_TO_CLIENT.direction())
                .encoder(SanSyncMessage::encode)
                .decoder(SanSyncMessage::decode)
                .consumerMainThread(TlipocaNetwork::handleSanSync)
                .add();
        CHANNEL.messageBuilder(OpenOracleScreenMessage.class, 1)
                .direction(NetworkDirection.PLAY_TO_CLIENT.direction())
                .encoder(OpenOracleScreenMessage::encode)
                .decoder(OpenOracleScreenMessage::decode)
                .consumerMainThread(TlipocaNetwork::handleOpenOracleScreen)
                .add();
        CHANNEL.messageBuilder(OpenYardLedgerScreenMessage.class, 2)
                .direction(NetworkDirection.PLAY_TO_CLIENT.direction())
                .encoder(OpenYardLedgerScreenMessage::encode)
                .decoder(OpenYardLedgerScreenMessage::decode)
                .consumerMainThread(TlipocaNetwork::handleOpenYardLedgerScreen)
                .add();
        CHANNEL.messageBuilder(OpenBountyBoardScreenMessage.class, 3)
                .direction(NetworkDirection.PLAY_TO_CLIENT.direction())
                .encoder(OpenBountyBoardScreenMessage::encode)
                .decoder(OpenBountyBoardScreenMessage::decode)
                .consumerMainThread(TlipocaNetwork::handleOpenBountyBoardScreen)
                .add();
        CHANNEL.messageBuilder(ClaimBountyMessage.class, 4)
                .direction(NetworkDirection.PLAY_TO_SERVER.direction())
                .encoder(ClaimBountyMessage::encode)
                .decoder(ClaimBountyMessage::decode)
                .consumerMainThread(TlipocaNetwork::handleClaimBounty)
                .add();
        CHANNEL.build();
    }

    public static void syncSan(ServerPlayer player, int san, boolean forbiddenActive) {
        CHANNEL.send(new SanSyncMessage(san, forbiddenActive), PacketDistributor.PLAYER.with(player));
    }

    public static void openOracleScreen(ServerPlayer player) {
        PlayerOracleData data = OracleManager.getData(player);
        int[] stars = new int[] {
                OracleManager.getOracleStar(data, "dagon_gold"),
                OracleManager.getOracleStar(data, "earth_vein_memory"),
                OracleManager.getOracleStar(data, "hunter_gaze"),
                OracleManager.getOracleStar(data, "forbidden_vitality"),
                OracleManager.getOracleStar(data, "mist_step"),
                OracleManager.getOracleStar(data, "abyss_echo")
        };
        CHANNEL.send(new OpenOracleScreenMessage(stars), PacketDistributor.PLAYER.with(player));
    }

    public static void openYardLedgerScreen(ServerPlayer player) {
        PlayerOracleData data = OracleManager.getData(player);
        int unlockedOracleCount = countUnlockedOracles(data);
        YardManager.YardProfile profile = YardManager.getYardProfile(player);
        YardManager.YardStage stage = YardManager.getYardStage(profile);
        YardManager.YardSoulContainerSummary soulContainers = YardManager.getSoulContainerSummary(player);
        MistVisitorRequest visitorRequest = MistVisitorRequest.currentFor(player, profile);
        CHANNEL.send(new OpenYardLedgerScreenMessage(
            YardManager.isInYard(player),
            data.getSan(),
            unlockedOracleCount,
            data.isFirstFullScytheReleaseSeen(),
            data.getTotalSoulsReleased(),
            profile.comfort(),
            profile.otherworld(),
            profile.memory(),
            stage.displayName(),
            stage.description(),
            stage.hint(),
            YardManager.hasMistNightLetter(player, profile),
            data.getMistLettersAnswered(),
            YardManager.hasMistNightVisitor(player, profile),
            data.getMistVisitorsHelped(),
            MistVisitorRequest.countRecordedTypes(data.getMistVisitorRecords()),
            MistVisitorRequest.totalRequestTypes(),
            MistVisitorRequest.summarizeRecords(data.getMistVisitorRecords()),
            visitorRequest.title(),
            visitorRequest.record(),
            visitorRequest.requestText(),
            visitorRequest.rewardText(),
            soulContainers.containerCount(),
            soulContainers.storedSouls(),
            soulContainers.maxSouls()
        ), PacketDistributor.PLAYER.with(player));
    }

    public static void openBountyBoardScreen(ServerPlayer player, BlockPos boardPos) {
        Level level = player.level();
        if (!(level.getBlockEntity(boardPos) instanceof BountyBoardBlockEntity board)) {
            return;
        }
        board.refreshIfNeeded(level);
        String[] bountyIds = new String[BountyManager.SLOT_COUNT];
        boolean[] claimed = new boolean[BountyManager.SLOT_COUNT];
        for (int slot = 0; slot < BountyManager.SLOT_COUNT; slot++) {
            bountyIds[slot] = board.getBountyId(slot);
            claimed[slot] = board.isClaimed(slot);
        }
        CHANNEL.send(new OpenBountyBoardScreenMessage(boardPos, bountyIds, claimed, board.getTicksUntilRefresh(level)), PacketDistributor.PLAYER.with(player));
    }

    public static void claimBounty(BlockPos boardPos, int bountyIndex) {
        CHANNEL.send(new ClaimBountyMessage(boardPos, bountyIndex), PacketDistributor.SERVER.noArg());
    }

    private static int countUnlockedOracles(PlayerOracleData data) {
        int unlocked = 0;
        if (OracleManager.getOracleStar(data, "dagon_gold") > 0) {
            unlocked++;
        }
        if (OracleManager.getOracleStar(data, "earth_vein_memory") > 0) {
            unlocked++;
        }
        if (OracleManager.getOracleStar(data, "hunter_gaze") > 0) {
            unlocked++;
        }
        if (OracleManager.getOracleStar(data, "forbidden_vitality") > 0) {
            unlocked++;
        }
        if (OracleManager.getOracleStar(data, "mist_step") > 0) {
            unlocked++;
        }
        if (OracleManager.getOracleStar(data, "abyss_echo") > 0) {
            unlocked++;
        }
        return unlocked;
    }

    private static void handleSanSync(SanSyncMessage message, CustomPayloadEvent.Context context) {
        if (!context.isClientSide()) {
            return;
        }

        context.enqueueWork(() -> io.tlipoca.mod.client.ClientSanSyncHandler.handle(message));
    }

    private static void handleOpenOracleScreen(OpenOracleScreenMessage message, CustomPayloadEvent.Context context) {
        if (!context.isClientSide()) {
            return;
        }

        context.enqueueWork(() -> io.tlipoca.mod.client.OracleScreenOpener.open(message));
    }

    private static void handleOpenYardLedgerScreen(OpenYardLedgerScreenMessage message, CustomPayloadEvent.Context context) {
        if (!context.isClientSide()) {
            return;
        }

        context.enqueueWork(() -> io.tlipoca.mod.client.YardLedgerScreenOpener.open(message));
    }

    private static void handleOpenBountyBoardScreen(OpenBountyBoardScreenMessage message, CustomPayloadEvent.Context context) {
        if (!context.isClientSide()) {
            return;
        }

        context.enqueueWork(() -> io.tlipoca.mod.client.BountyBoardScreenOpener.open(message));
    }

    private static void handleClaimBounty(ClaimBountyMessage message, CustomPayloadEvent.Context context) {
        if (!context.isServerSide()) {
            return;
        }

        context.enqueueWork(() -> {
            if (!(context.getSender() instanceof ServerPlayer player)) {
                return;
            }
            Level level = player.level();
            if (!(level.getBlockEntity(message.boardPos()) instanceof BountyBoardBlockEntity board)) {
                return;
            }
            board.refreshIfNeeded(level);
            int slot = message.bountyIndex();
            if (slot < 0 || slot >= BountyManager.SLOT_COUNT || board.isClaimed(slot)) {
                return;
            }

            ItemStack contract = BountyManager.createContract(board.getBountyId(slot), level.getGameTime());
            if (!player.getInventory().add(contract)) {
                player.drop(contract, false);
            }
            board.claim(slot);
            openBountyBoardScreen(player, message.boardPos());
        });
    }
}
