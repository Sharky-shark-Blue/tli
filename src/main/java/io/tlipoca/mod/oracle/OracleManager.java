package io.tlipoca.mod.oracle;

import com.mojang.logging.LogUtils;
import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.network.TlipocaNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OracleManager {
    public static final int DIVINATION_SAN_COST = 5;
    public static final int MAX_ORACLE_STAR = 5;
    public static final int ORDINARY_ORACLE_KIND_LIMIT = 12;
    public static final int ORACLE_CYCLE_DAYS = 7;
    public static final int DIVINATION_COOLDOWN_TICKS = 40;
    public static final int DAGON_WATER_GRACE_TICKS = 6000;
    public static final int DAGON_FOOD_DRAIN_INTERVAL = 60;
    public static final int EARTH_VEIN_RADIUS = 5;
    public static final int EARTH_VEIN_PARTICLE_DURATION_TICKS = 20 * 10;
    public static final int EARTH_VEIN_PARTICLE_INTERVAL = 10;
    public static final int HUNTER_GAZE_DURATION_TICKS = 160;
    public static final int HUNTER_GAZE_MAX_STACKS = 3;
    public static final int MIST_STEP_PARTICLE_INTERVAL = 200;
    private static final int SAN_DECAY_DAY = 1200;
    private static final int SAN_DECAY_NIGHT = 600;
    private static final int SAN_DECAY_NETHER = 400;
    private static final int SAN_DECAY_END = 400;
    private static final int SAN_RESTORE_SUNLIGHT = 2400;
    private static final int SAN_RESTORE_SLEEP = 10;
    private static final int SAN_DECAY_MIN = 1;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ORACLE_DATA_PREFIX = "tlipoca_oracle_data";
    private static final Map<String, OracleDefinition> ORACLES = new LinkedHashMap<>();
    private static final int SUPREME_HINT_THRESHOLD = 10;
    private static final Map<UUID, List<BlockPos>> EARTH_VEIN_MARKERS = new HashMap<>();
    private static final Map<UUID, Long> EARTH_VEIN_MARKER_EXPIRY = new HashMap<>();
    private static final Map<UUID, BlockPos> SUNLIGHT_STANDING_POS = new HashMap<>();
    private static final Map<UUID, Long> SUNLIGHT_STANDING_START = new HashMap<>();
    private static final ResourceLocation[] HUNTER_GAZE_MODIFIER_IDS = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath(TlipocaMod.MODID, "hunter_gaze_0"),
            ResourceLocation.fromNamespaceAndPath(TlipocaMod.MODID, "hunter_gaze_1"),
            ResourceLocation.fromNamespaceAndPath(TlipocaMod.MODID, "hunter_gaze_2")
    };
    private static final Map<UUID, long[]> HUNTER_GAZE_EXPIRY = new HashMap<>();

    static {
        register(new OracleDefinition("dagon_gold", "达贡的黄金"));
        register(new OracleDefinition("earth_vein_memory", "地脉记忆"));
        register(new OracleDefinition("hunter_gaze", "猎者凝视"));
        register(new OracleDefinition("forbidden_vitality", "禁忌活力"));
        register(new OracleDefinition("mist_step", "雾中步伐"));
        register(new OracleDefinition("abyss_echo", "深渊回响"));
    }

    private OracleManager() {
    }

    private static void register(OracleDefinition definition) {
        ORACLES.put(definition.getId(), definition);
    }

    public static PlayerOracleData getData(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        PlayerOracleData data = new PlayerOracleData();
        data.load(root, ORACLE_DATA_PREFIX);
        return data;
    }

    public static void saveData(ServerPlayer player, PlayerOracleData data) {
        data.save(player.getPersistentData(), ORACLE_DATA_PREFIX);
    }

    public static void syncSan(ServerPlayer player) {
        PlayerOracleData data = getData(player);
        TlipocaNetwork.syncSan(player, data.getSan(), data.isForbiddenActive());
    }

    public static boolean rollChance(ServerPlayer player, float chance) {
        return chance > 0.0F && player.getRandom().nextFloat() < chance;
    }

    public static boolean isCreativeDebugPlayer(ServerPlayer player) {
        return player.getAbilities().instabuild;
    }

    public static PlayerOracleData ensureCreativeSan(ServerPlayer player) {
        PlayerOracleData data = getData(player);
        if (isCreativeDebugPlayer(player) && data.getSan() != 100) {
            data.setSan(100);
            saveData(player, data);
            syncSan(player);
        }
        return data;
    }

    public static List<OracleDefinition> getAvailableOracles(PlayerOracleData.SanTier tier) {
        return new ArrayList<>(ORACLES.values());
    }

    public static long getCurrentDay(ServerPlayer player) {
        return player.level().getDayTime() / 24000L;
    }

    public static long getCycleIndex(long day) {
        return day / ORACLE_CYCLE_DAYS;
    }

    public static OracleDefinition getOracle(String oracleId) {
        return ORACLES.get(oracleId);
    }

    public static String getOracleDisplayName(String oracleId) {
        OracleDefinition definition = ORACLES.get(oracleId);
        return definition == null ? oracleId : definition.getDisplayName();
    }

    public static int getOracleStar(PlayerOracleData data, String oracleId) {
        return data.getOracleStars().getOrDefault(oracleId, 0);
    }

    public static boolean rollStarChance(ServerPlayer player, int star) {
        return rollChance(player, star * 0.05F);
    }

    public static String romanStar(int star) {
        return switch (star) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(star);
        };
    }

    public static boolean hasOracle(PlayerOracleData data, String oracleId) {
        return data.getActiveOracles().contains(oracleId) && getOracleStar(data, oracleId) > 0;
    }

    public static boolean triggerHunterGaze(ServerPlayer player, PlayerOracleData data, long gameTime) {
        int star = getOracleStar(data, "hunter_gaze");
        if (star <= 0 || !rollChance(player, star * 0.08F)) {
            return false;
        }

        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return false;
        }

        long[] expirySlots = HUNTER_GAZE_EXPIRY.computeIfAbsent(player.getUUID(), ignored -> new long[HUNTER_GAZE_MAX_STACKS]);
        int chosenSlot = -1;
        for (int index = 0; index < HUNTER_GAZE_MAX_STACKS; index++) {
            if (expirySlots[index] <= gameTime) {
                chosenSlot = index;
                break;
            }
        }
        if (chosenSlot == -1) {
            int earliestIndex = 0;
            for (int index = 1; index < HUNTER_GAZE_MAX_STACKS; index++) {
                if (expirySlots[index] < expirySlots[earliestIndex]) {
                    earliestIndex = index;
                }
            }
            chosenSlot = earliestIndex;
        }

        attackDamage.addOrUpdateTransientModifier(
                new AttributeModifier(HUNTER_GAZE_MODIFIER_IDS[chosenSlot], 2.0D, AttributeModifier.Operation.ADD_VALUE)
        );
        expirySlots[chosenSlot] = gameTime + HUNTER_GAZE_DURATION_TICKS;

        if (!isCreativeDebugPlayer(player)) {
            data.consumeSan(1);
        }
        saveData(player, data);
        resetSanZeroStateIfRecovered(player);
        syncSan(player);
        if (data.getSan() == 0) {
            triggerSanZeroEffect(player, data);
            saveData(player, data);
            syncSan(player);
        }

        LOGGER.info(
                "oracle_effect hunter_gaze player={} star={} slot={} san={}",
                player.getName().getString(),
                star,
                chosenSlot,
                data.getSan()
        );
        return true;
    }

    public static void tickHunterGaze(ServerPlayer player, long gameTime) {
        long[] expirySlots = HUNTER_GAZE_EXPIRY.get(player.getUUID());
        if (expirySlots == null) {
            return;
        }
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }

        boolean anyActive = false;
        for (int index = 0; index < HUNTER_GAZE_MAX_STACKS; index++) {
            if (expirySlots[index] > gameTime) {
                anyActive = true;
                continue;
            }
            attackDamage.removeModifier(HUNTER_GAZE_MODIFIER_IDS[index]);
        }
        if (!anyActive) {
            HUNTER_GAZE_EXPIRY.remove(player.getUUID());
        }
    }

    public static boolean updateForbiddenVitality(ServerPlayer player, PlayerOracleData data) {
        boolean strengthActive = hasOracle(data, "forbidden_vitality") && data.getSan() <= 30;
        if (strengthActive) {
            boolean wasActive = data.isForbiddenActive();
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 0, true, false));
            if (data.getSan() <= 15) {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false));
            }
            data.setForbiddenActive(true);
            saveData(player, data);
            syncSan(player);
            return !wasActive;
        }
        if (!data.isForbiddenActive()) {
            return false;
        }
        data.setForbiddenActive(false);
        saveData(player, data);
        syncSan(player);
        return true;
    }

    public static void tickMistStep(ServerPlayer player, long gameTime) {
        if (!hasOracle(getData(player), "mist_step") || !isNight(player.level()) || !player.level().canSeeSky(player.blockPosition())) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0, true, false));
        if (!(player.level() instanceof ServerLevel level) || gameTime % MIST_STEP_PARTICLE_INTERVAL != 0L) {
            return;
        }
        double particleX = player.getX() + (player.getRandom().nextDouble() * 16.0D - 8.0D);
        double particleZ = player.getZ() + (player.getRandom().nextDouble() * 16.0D - 8.0D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, particleX, player.getY(), particleZ, 8, 0.4D, 0.6D, 0.4D, 0.01D);
    }

    public static boolean tickNaturalSanDecay(ServerPlayer player, PlayerOracleData data, long gameTime) {
        if (isCreativeDebugPlayer(player) || data.getSan() <= SAN_DECAY_MIN) {
            return false;
        }

        int interval = getNaturalSanDecayInterval(player.level());
        if (interval <= 0 || gameTime % interval != 0L) {
            return false;
        }

        data.setSan(Math.max(SAN_DECAY_MIN, data.getSan() - 1));
        return true;
    }

    public static boolean tickSunlightSanRestore(ServerPlayer player, PlayerOracleData data, long gameTime) {
        UUID playerId = player.getUUID();
        if (isCreativeDebugPlayer(player) || data.getSan() >= 100 || !isOverworldDaytime(player.level())
                || !player.level().canSeeSky(player.blockPosition())) {
            SUNLIGHT_STANDING_POS.remove(playerId);
            SUNLIGHT_STANDING_START.remove(playerId);
            return false;
        }

        BlockPos currentPos = player.blockPosition();
        BlockPos standingPos = SUNLIGHT_STANDING_POS.get(playerId);
        if (!currentPos.equals(standingPos)) {
            SUNLIGHT_STANDING_POS.put(playerId, currentPos.immutable());
            SUNLIGHT_STANDING_START.put(playerId, gameTime);
            return false;
        }

        long startTime = SUNLIGHT_STANDING_START.getOrDefault(playerId, gameTime);
        if (gameTime - startTime < SAN_RESTORE_SUNLIGHT) {
            return false;
        }

        data.setSan(data.getSan() + 1);
        SUNLIGHT_STANDING_START.put(playerId, gameTime);
        return true;
    }

    public static boolean restoreSanFromSleep(ServerPlayer player) {
        if (isCreativeDebugPlayer(player)) {
            return false;
        }

        PlayerOracleData data = getData(player);
        long gameTime = player.level().getGameTime();
        long lastSleepRestoreTime = data.getLastSleepRestoreTime();
        if (lastSleepRestoreTime == gameTime) {
            return false;
        }
        data.setLastSleepRestoreTime(gameTime);
        if (data.getSan() >= 100) {
            saveData(player, data);
            return false;
        }

        data.setSan(data.getSan() + SAN_RESTORE_SLEEP);
        saveData(player, data);
        syncSan(player);
        return true;
    }

    public static boolean applyDagonGold(ServerPlayer player, PlayerOracleData data) {
        if (!hasOracle(data, "dagon_gold") || !player.isInWater()) {
            return false;
        }
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 1, true, false));
        if (data.getLastLeftWaterTime() != -1L) {
            data.setLastLeftWaterTime(-1L);
            return true;
        }
        return false;
    }

    public static boolean tickDagonGold(ServerPlayer player, PlayerOracleData data, long gameTime) {
        long lastLeftWaterTime = data.getLastLeftWaterTime();
        if (!hasOracle(data, "dagon_gold")) {
            if (lastLeftWaterTime != -1L) {
                data.setLastLeftWaterTime(-1L);
                return true;
            }
            return false;
        }
        if (player.isInWater()) {
            if (lastLeftWaterTime != -1L) {
                data.setLastLeftWaterTime(-1L);
                return true;
            }
            return false;
        }
        if (lastLeftWaterTime == -1L) {
            data.setLastLeftWaterTime(gameTime);
            return true;
        }
        return false;
    }

    public static void tickDagonGoldPenalty(ServerPlayer player, PlayerOracleData data, long gameTime) {
        if (!hasOracle(data, "dagon_gold")) {
            return;
        }
        long lastLeftWaterTime = data.getLastLeftWaterTime();
        if (lastLeftWaterTime == -1L) {
            return;
        }
        long delta = gameTime - lastLeftWaterTime;
        if (delta < DAGON_WATER_GRACE_TICKS || gameTime % DAGON_FOOD_DRAIN_INTERVAL != 0L) {
            return;
        }
        int foodLevel = player.getFoodData().getFoodLevel();
        if (foodLevel > 0) {
            player.getFoodData().setFoodLevel(foodLevel - 1);
        }
    }

    public static boolean triggerEarthVeinMemory(ServerPlayer player, ServerLevel level, PlayerOracleData data, BlockPos origin, BlockState minedState) {
        int star = getOracleStar(data, "earth_vein_memory");
        if (star <= 0 || !rollChance(player, star * 0.12F)) {
            return false;
        }

        List<BlockPos> foundPositions = new ArrayList<>();
        for (int offsetX = -EARTH_VEIN_RADIUS; offsetX <= EARTH_VEIN_RADIUS; offsetX++) {
            for (int offsetY = -EARTH_VEIN_RADIUS; offsetY <= EARTH_VEIN_RADIUS; offsetY++) {
                for (int offsetZ = -EARTH_VEIN_RADIUS; offsetZ <= EARTH_VEIN_RADIUS; offsetZ++) {
                    BlockPos targetPos = origin.offset(offsetX, offsetY, offsetZ);
                    if (targetPos.equals(origin)) {
                        continue;
                    }
                    if (level.getBlockState(targetPos).is(minedState.getBlock())) {
                        foundPositions.add(targetPos.immutable());
                    }
                }
            }
        }

        if (foundPositions.isEmpty()) {
            return false;
        }

        UUID playerId = player.getUUID();
        EARTH_VEIN_MARKERS.put(playerId, foundPositions);
        EARTH_VEIN_MARKER_EXPIRY.put(playerId, level.getGameTime() + EARTH_VEIN_PARTICLE_DURATION_TICKS);

        if (!isCreativeDebugPlayer(player)) {
            data.consumeSan(1);
        }
        saveData(player, data);
        syncSan(player);

        LOGGER.info(
                "oracle_effect earth_vein_memory player={} star={} origin={} markers={} san={}",
                player.getName().getString(),
                star,
                origin,
                foundPositions.size(),
                data.getSan()
        );
        renderEarthVeinParticles(level, foundPositions);
        if (data.getSan() == 0) {
            triggerSanZeroEffect(player, data);
            saveData(player, data);
            syncSan(player);
        }
        return true;
    }

    public static void tickEarthVeinMarkers(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Long expiryTime = EARTH_VEIN_MARKER_EXPIRY.get(playerId);
        if (expiryTime == null) {
            return;
        }
        long gameTime = player.level().getGameTime();
        if (gameTime >= expiryTime) {
            EARTH_VEIN_MARKER_EXPIRY.remove(playerId);
            EARTH_VEIN_MARKERS.remove(playerId);
            return;
        }
        if (!(player.level() instanceof ServerLevel level) || gameTime % EARTH_VEIN_PARTICLE_INTERVAL != 0L) {
            return;
        }
        List<BlockPos> markerPositions = EARTH_VEIN_MARKERS.get(playerId);
        if (markerPositions != null && !markerPositions.isEmpty()) {
            renderEarthVeinParticles(level, markerPositions);
        }
    }

    private static void renderEarthVeinParticles(ServerLevel level, List<BlockPos> markerPositions) {
        for (BlockPos markerPos : markerPositions) {
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    markerPos.getX() + 0.5D,
                    markerPos.getY() + 0.5D,
                    markerPos.getZ() + 0.5D,
                    5,
                    0.2D,
                    0.2D,
                    0.2D,
                    0.01D
            );
        }
    }

    public static void tryDivine(ServerPlayer player) {
        PlayerOracleData data = getData(player);
        boolean creativeDebug = isCreativeDebugPlayer(player);
        if (creativeDebug && data.getSan() != 100) {
            data.setSan(100);
        }
        long gameTime = player.level().getGameTime();
        long day = getCurrentDay(player);
        long cycleIndex = getCycleIndex(day);

        settleCycleIfNeeded(player, data, cycleIndex);

        if (!creativeDebug && gameTime - data.getLastDivinationGameTime() < DIVINATION_COOLDOWN_TICKS) {
            LOGGER.info("ordinary_oracle_divination_rejected player={} uuid={} reason=cooldown", player.getName().getString(), player.getUUID());
            player.sendSystemMessage(Component.literal("群星尚未重新排列。"));
            saveData(player, data);
            return;
        }

        if (!creativeDebug && data.getLastDivinationDay() == day) {
            LOGGER.info("ordinary_oracle_divination_rejected player={} uuid={} reason=already_divined_today day={}", player.getName().getString(), player.getUUID(), day);
            player.sendSystemMessage(Component.literal("今日的神谕已经昭现。"));
            saveData(player, data);
            return;
        }

        if (!creativeDebug && data.getSan() < DIVINATION_SAN_COST) {
            LOGGER.info("ordinary_oracle_divination_rejected player={} uuid={} reason=low_san san={}", player.getName().getString(), player.getUUID(), data.getSan());
            player.sendSystemMessage(Component.literal("你的理智不足以承载新的神谕。"));
            saveData(player, data);
            syncSan(player);
            return;
        }

        OracleDefinition chosen = chooseOrdinaryOracle(player, data);
        if (!creativeDebug) {
            data.setLastDivinationGameTime(gameTime);
        }
        if (chosen == null) {
            if (!creativeDebug) {
                data.setLastDivinationDay(day);
            }
            player.sendSystemMessage(Component.literal("群星沉默不语。"));
            saveData(player, data);
            syncSan(player);
            return;
        }

        int sanBefore = data.getSan();
        if (!creativeDebug) {
            data.consumeSan(DIVINATION_SAN_COST);
            data.setLastDivinationDay(day);
        }
        data.getActiveOracles().add(chosen.getId());
        int oldStar = getOracleStar(data, chosen.getId());
        int star = data.upgradeStar(chosen.getId());
        data.incrementOracleCountThisCycle();

        boolean shieldGranted = false;
        int abyssStar = getOracleStar(data, "abyss_echo");
        if (abyssStar > 0 && rollChance(player, abyssStar * 0.08F)) {
            data.setAbyssShield(true);
            shieldGranted = true;
        }
        if (creativeDebug) {
            data.setSan(100);
        }

        if (data.getSan() == 0) {
            triggerSanZeroEffect(player, data);
        }

        saveData(player, data);
        syncSan(player);

        LOGGER.info(
                "ordinary_oracle_divination player={} uuid={} mode={} day={} cycle={} san_before={} san_after={} oracle_id={} star={} oracle_count_this_cycle={} refunded={}",
                player.getName().getString(),
                player.getUUID(),
                creativeDebug ? "creative_debug" : "survival",
                day,
                cycleIndex,
                sanBefore,
                data.getSan(),
                chosen.getId(),
                star,
                data.getOracleCountThisCycle(),
                shieldGranted
        );

        String starText = romanStar(star);
        if (creativeDebug) {
            if (oldStar >= MAX_ORACLE_STAR) {
                player.sendSystemMessage(Component.literal("创造调试：神谕已臻满星：" + chosen.getDisplayName() + " " + starText));
            } else if (oldStar > 0) {
                player.sendSystemMessage(Component.literal("创造调试：神谕升格：" + chosen.getDisplayName() + " " + starText));
            } else {
                player.sendSystemMessage(Component.literal("创造调试：你获得了神谕：" + chosen.getDisplayName() + " " + starText));
            }
            return;
        }

        if (oldStar >= MAX_ORACLE_STAR) {
            player.sendSystemMessage(Component.literal("神谕已臻满星：" + chosen.getDisplayName() + " " + starText));
        } else if (oldStar > 0) {
            player.sendSystemMessage(Component.literal("神谕升格：" + chosen.getDisplayName() + " " + starText));
        } else {
            player.sendSystemMessage(Component.literal("你获得了神谕：" + chosen.getDisplayName() + " " + starText));
        }
        player.sendSystemMessage(Component.literal("SAN：" + sanBefore + " -> " + data.getSan()));
    }

    private static void settleCycleIfNeeded(ServerPlayer player, PlayerOracleData data, long cycleIndex) {
        if (data.getCurrentCycleIndex() == -1) {
            data.setCurrentCycleIndex(cycleIndex);
            return;
        }
        if (data.getCurrentCycleIndex() == cycleIndex) {
            return;
        }

        if (data.getOracleCountThisCycle() >= SUPREME_HINT_THRESHOLD) {
            player.displayClientMessage(Component.literal("你感到某个至高存在回应了凝视。"), false);
            LOGGER.info(
                    "supreme_oracle_hint player={} uuid={} settled_cycle={} oracle_count_this_cycle={}",
                    player.getName().getString(),
                    player.getUUID(),
                    data.getCurrentCycleIndex(),
                    data.getOracleCountThisCycle()
            );
        }

        data.clearOrdinaryOracles();
        data.resetOracleCountThisCycle();
        data.setCurrentCycleIndex(cycleIndex);
        player.sendSystemMessage(Component.literal("血月的阴影吞噬了旧日神谕。"));
    }

    private static OracleDefinition chooseOrdinaryOracle(ServerPlayer player, PlayerOracleData data) {
        List<OracleDefinition> candidates = new ArrayList<>();
        boolean kindLimitReached = data.getActiveOracles().size() >= ORDINARY_ORACLE_KIND_LIMIT;
        for (OracleDefinition definition : ORACLES.values()) {
            int star = getOracleStar(data, definition.getId());
            if (star >= MAX_ORACLE_STAR) {
                continue;
            }
            if (!kindLimitReached || data.getActiveOracles().contains(definition.getId())) {
                candidates.add(definition);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(player.getRandom().nextInt(candidates.size()));
    }

    public static void triggerSanZeroEffect(ServerPlayer player, PlayerOracleData data) {
        if (data.isSanZeroTriggered()) {
            return;
        }
        data.setSanZeroTriggered(true);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0));
        teleportNearPlayer(player);
        data.setSan(15);
        data.setSanZeroTriggered(false);
    }

    public static void resetSanZeroStateIfRecovered(ServerPlayer player) {
        PlayerOracleData data = getData(player);
        if (data.getSan() > 0 && data.isSanZeroTriggered()) {
            data.setSanZeroTriggered(false);
            saveData(player, data);
        }
    }

    private static boolean isNight(Level level) {
        long dayTime = level.getDayTime() % 24000L;
        return dayTime >= 13000L && dayTime <= 23000L;
    }

    private static boolean isOverworldDaytime(Level level) {
        long dayTime = level.getDayTime() % 24000L;
        return level.dimension() == Level.OVERWORLD && dayTime >= 0L && dayTime < 12000L;
    }

    private static int getNaturalSanDecayInterval(Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            long dayTime = level.getDayTime() % 24000L;
            return dayTime < 12000L ? SAN_DECAY_DAY : SAN_DECAY_NIGHT;
        }
        if (level.dimension() == Level.NETHER) {
            return SAN_DECAY_NETHER;
        }
        if (level.dimension() == Level.END) {
            return SAN_DECAY_END;
        }
        return 0;
    }

    private static void teleportNearPlayer(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        for (int attempt = 0; attempt < 12; attempt++) {
            int offsetX = player.getRandom().nextInt(61) - 30;
            int offsetZ = player.getRandom().nextInt(61) - 30;
            int targetX = player.blockPosition().getX() + offsetX;
            int targetZ = player.blockPosition().getZ() + offsetZ;
            BlockPos safeGround = findSafeGround(level, targetX, targetZ);
            if (safeGround != null) {
                player.teleportTo(level, safeGround.getX() + 0.5D, safeGround.getY() + 1.0D, safeGround.getZ() + 0.5D, java.util.Set.of(), player.getYRot(), player.getXRot(), true);
                return;
            }
        }
    }

    private static BlockPos findSafeGround(ServerLevel level, int targetX, int targetZ) {
        for (int targetY = 255; targetY >= level.getMinY(); targetY--) {
            BlockPos groundPos = new BlockPos(targetX, targetY, targetZ);
            BlockState groundState = level.getBlockState(groundPos);
            FluidState groundFluid = level.getFluidState(groundPos);
            if (groundState.isAir() || !groundFluid.isEmpty()) {
                continue;
            }
            BlockPos feetPos = groundPos.above();
            BlockPos headPos = feetPos.above();
            if (level.getBlockState(feetPos).isAir() && level.getBlockState(headPos).isAir()) {
                return groundPos;
            }
        }
        return null;
    }
}
