package io.tlipoca.mod.yard;

import io.tlipoca.mod.TlipocaMod;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;

public final class YardManager {
    private static final double YARD_RADIUS = 32.0D;
    private static final double YARD_RADIUS_SQR = YARD_RADIUS * YARD_RADIUS;
    private static final Map<ResourceKey<Level>, Set<BlockPos>> ANCHORS = new HashMap<>();

    private YardManager() {
    }

    public static void register(ServerLevel level, BlockPos pos) {
        ANCHORS.computeIfAbsent(level.dimension(), dimension -> new HashSet<>()).add(pos.immutable());
    }

    public static void unregister(ServerLevel level, BlockPos pos) {
        Set<BlockPos> anchors = ANCHORS.get(level.dimension());
        if (anchors == null) {
            return;
        }
        anchors.remove(pos);
        if (anchors.isEmpty()) {
            ANCHORS.remove(level.dimension());
        }
    }

    public static boolean isInYard(ServerPlayer player) {
        Set<BlockPos> anchors = ANCHORS.get(player.level().dimension());
        if (anchors == null || anchors.isEmpty()) {
            return false;
        }

        for (BlockPos anchor : anchors) {
            if (player.distanceToSqr(anchor.getX() + 0.5D, anchor.getY() + 0.5D, anchor.getZ() + 0.5D) <= YARD_RADIUS_SQR) {
                return true;
            }
        }
        return false;
    }

    public static YardSurvey survey(ServerLevel level, BlockPos center) {
        int anchors = 0;
        int alchemyTables = 0;
        int offeringPedestals = 0;
        int radius = (int) YARD_RADIUS;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -8, -radius), center.offset(radius, 8, radius))) {
            Block block = level.getBlockState(pos).getBlock();
            if (block == TlipocaMod.YARD_ANCHOR.get()) {
                anchors++;
            } else if (block == TlipocaMod.YARD_ALCHEMY_TABLE.get()) {
                alchemyTables++;
            } else if (block == TlipocaMod.YARD_OFFERING_PEDESTAL.get()) {
                offeringPedestals++;
            }
        }

        return new YardSurvey(radius, anchors, alchemyTables, offeringPedestals);
    }

    public record YardSurvey(int radius, int anchors, int alchemyTables, int offeringPedestals) {
        public boolean hasCompleteAlchemySet() {
            return alchemyTables > 0 && offeringPedestals >= 3;
        }
    }
}
