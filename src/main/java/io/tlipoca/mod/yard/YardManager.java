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

    public static YardProfile getYardProfile(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return YardProfile.EMPTY;
        }

        BlockPos center = findContainingAnchor(player);
        if (center == null) {
            center = player.blockPosition();
        }

        int comfort = 0;
        int otherworld = 0;
        int memory = 0;
        int radius = (int) YARD_RADIUS;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -8, -radius), center.offset(radius, 8, radius))) {
            if (!isWithinYardRadius(center, pos)) {
                continue;
            }

            Block block = level.getBlockState(pos).getBlock();
            if (block == TlipocaMod.MIST_LAMP.get()) {
                comfort += 1;
                otherworld += 2;
            } else if (block == TlipocaMod.OLD_POSTER.get()) {
                otherworld += 1;
                memory += 2;
            } else if (block == TlipocaMod.NAMELESS_FLOWER.get()) {
                otherworld += 1;
                memory += 1;
            }
        }

        return new YardProfile(comfort, otherworld, memory);
    }

    public static YardStage getYardStage(YardProfile profile) {
        if (profile.comfort() >= 3 && profile.otherworld() >= 6 && profile.memory() >= 5) {
            return YardStage.STABLE_BORDER;
        }
        if (profile.otherworld() >= 5 && profile.memory() >= 3) {
            return YardStage.INVITED;
        }
        if (profile.otherworld() >= 3) {
            return YardStage.NOTICED;
        }
        return YardStage.STRANGE;
    }

    public static boolean hasMistNightLetter(ServerPlayer player, YardProfile profile) {
        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!isInYard(player) || level.dimension() != Level.OVERWORLD) {
            return false;
        }

        long dayTime = level.getDayTime() % 24000L;
        boolean isNight = dayTime >= 13000L && dayTime <= 23000L;
        boolean isInvitedOrBetter = profile.otherworld() >= 5 && profile.memory() >= 3;
        return isNight && isInvitedOrBetter;
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

    private static BlockPos findContainingAnchor(ServerPlayer player) {
        Set<BlockPos> anchors = ANCHORS.get(player.level().dimension());
        if (anchors == null || anchors.isEmpty()) {
            return null;
        }

        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (BlockPos anchor : anchors) {
            double distance = player.distanceToSqr(anchor.getX() + 0.5D, anchor.getY() + 0.5D, anchor.getZ() + 0.5D);
            if (distance <= YARD_RADIUS_SQR && distance < nearestDistance) {
                nearest = anchor;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static boolean isWithinYardRadius(BlockPos center, BlockPos pos) {
        double dx = pos.getX() + 0.5D - (center.getX() + 0.5D);
        double dy = pos.getY() + 0.5D - (center.getY() + 0.5D);
        double dz = pos.getZ() + 0.5D - (center.getZ() + 0.5D);
        return dx * dx + dy * dy + dz * dz <= YARD_RADIUS_SQR;
    }

    public record YardProfile(int comfort, int otherworld, int memory) {
        public static final YardProfile EMPTY = new YardProfile(0, 0, 0);
    }

    public enum YardStage {
        STRANGE(
            "陌生",
            "庭院还没有被记住。",
            "下一步：放置雾灯或无名花，让庭院被雾注意到。"
        ),
        NOTICED(
            "被注意",
            "雾开始认得这里的路。",
            "下一步：收集记忆碎片，或摆放旧海报。"
        ),
        INVITED(
            "被邀请",
            "有些名字正在靠近门口。",
            "下一步：提高舒适度，让来客愿意停留。"
        ),
        STABLE_BORDER(
            "稳定边境",
            "这里暂时像一个可以停留的地方。",
            "下一步：等待夜晚。这里也许会有人来。"
        );

        private final String displayName;
        private final String description;
        private final String hint;

        YardStage(String displayName, String description, String hint) {
            this.displayName = displayName;
            this.description = description;
            this.hint = hint;
        }

        public String displayName() {
            return displayName;
        }

        public String description() {
            return description;
        }

        public String hint() {
            return hint;
        }
    }

    public record YardSurvey(int radius, int anchors, int alchemyTables, int offeringPedestals) {
        public boolean hasCompleteAlchemySet() {
            return alchemyTables > 0 && offeringPedestals >= 3;
        }
    }
}
