package io.tlipoca.mod.oracle;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerOracleData {
    public enum SanTier {
        HIGH,
        MID_LOW,
        VERY_LOW,
        ZERO
    }

    private int san = 100;
    private final Set<String> activeOracles = new HashSet<>();
    private final Map<String, Integer> oracleStars = new HashMap<>();
    private int oracleFragments;
    private int oracleCountThisCycle;
    private long lastDivinationGameTime = -100;
    private long lastDivinationDay = -1;
    private long currentCycleIndex = -1;
    private boolean sanZeroTriggered;
    // forbidden_vitality: HUD 颜色标记
    private boolean forbiddenActive;
    // abyss_echo: 一次性护盾标记
    private boolean abyssShield;
    // dagon_gold: 最后一次离开水的游戏时间（-1 表示当前在水中或未记录）
    private long lastLeftWaterTime = -1;
    private long lastSleepRestoreTime = -1L;

    public int getSan() {
        return san;
    }

    public void setSan(int san) {
        this.san = Math.max(0, Math.min(100, san));
    }

    public boolean consumeSan(int amount) {
        if (amount <= 0) {
            return true;
        }
        if (san < amount) {
            return false;
        }
        setSan(san - amount);
        return true;
    }

    public SanTier getSanTier() {
        if (san == 0) {
            return SanTier.ZERO;
        }
        if (san >= 75) {
            return SanTier.HIGH;
        }
        if (san >= 50) {
            return SanTier.MID_LOW;
        }
        return SanTier.VERY_LOW;
    }

    public Set<String> getActiveOracles() {
        return activeOracles;
    }

    public Map<String, Integer> getOracleStars() {
        return oracleStars;
    }

    public int getOracleFragments() {
        return oracleFragments;
    }

    public void addOracleFragments(int amount) {
        this.oracleFragments = Math.max(0, this.oracleFragments + amount);
    }

    public int getOracleCountThisCycle() {
        return oracleCountThisCycle;
    }

    public void incrementOracleCountThisCycle() {
        oracleCountThisCycle++;
    }

    public void resetOracleCountThisCycle() {
        oracleCountThisCycle = 0;
    }

    public void setOracleCountThisCycle(int oracleCountThisCycle) {
        this.oracleCountThisCycle = Math.max(0, oracleCountThisCycle);
    }

    public int getBloodMoonCycleOracleCount() {
        return oracleCountThisCycle;
    }

    public void incrementBloodMoonCycleOracleCount() {
        incrementOracleCountThisCycle();
    }

    public void resetBloodMoonCycleOracleCount() {
        resetOracleCountThisCycle();
    }

    public long getLastDivinationGameTime() {
        return lastDivinationGameTime;
    }

    public void setLastDivinationGameTime(long lastDivinationGameTime) {
        this.lastDivinationGameTime = lastDivinationGameTime;
    }

    public long getLastDivinationDay() {
        return lastDivinationDay;
    }

    public void setLastDivinationDay(long lastDivinationDay) {
        this.lastDivinationDay = lastDivinationDay;
    }

    public long getCurrentCycleIndex() {
        return currentCycleIndex;
    }

    public void setCurrentCycleIndex(long currentCycleIndex) {
        this.currentCycleIndex = currentCycleIndex;
    }

    public boolean isSanZeroTriggered() {
        return sanZeroTriggered;
    }

    public void setSanZeroTriggered(boolean sanZeroTriggered) {
        this.sanZeroTriggered = sanZeroTriggered;
    }

    public boolean isForbiddenActive() {
        return forbiddenActive;
    }

    public void setForbiddenActive(boolean forbiddenActive) {
        this.forbiddenActive = forbiddenActive;
    }

    public boolean isAbyssShield() {
        return abyssShield;
    }

    public void setAbyssShield(boolean abyssShield) {
        this.abyssShield = abyssShield;
    }

    public long getLastLeftWaterTime() {
        return lastLeftWaterTime;
    }

    public void setLastLeftWaterTime(long lastLeftWaterTime) {
        this.lastLeftWaterTime = lastLeftWaterTime;
    }

    public long getLastSleepRestoreTime() {
        return lastSleepRestoreTime;
    }

    public void setLastSleepRestoreTime(long lastSleepRestoreTime) {
        this.lastSleepRestoreTime = lastSleepRestoreTime;
    }

    public int upgradeStar(String oracleId) {
        int next = Math.min(OracleManager.MAX_ORACLE_STAR, oracleStars.getOrDefault(oracleId, 0) + 1);
        oracleStars.put(oracleId, next);
        return next;
    }

    public void setStar(String oracleId, int star) {
        int clamped = Math.max(1, Math.min(OracleManager.MAX_ORACLE_STAR, star));
        activeOracles.add(oracleId);
        oracleStars.put(oracleId, clamped);
    }

    public void removeOracle(String oracleId) {
        activeOracles.remove(oracleId);
        oracleStars.remove(oracleId);
    }

    public void clearOrdinaryOracles() {
        activeOracles.clear();
        oracleStars.clear();
    }

    public void save(CompoundTag tag, String prefix) {
        tag.putInt(prefix + "_san", san);
        tag.putInt(prefix + "_oracle_fragments", oracleFragments);
        tag.putInt(prefix + "_oracle_count_this_cycle", oracleCountThisCycle);
        tag.putInt(prefix + "_blood_moon_cycle_oracle_count", oracleCountThisCycle);
        tag.putLong(prefix + "_last_divination_game_time", lastDivinationGameTime);
        tag.putLong(prefix + "_last_divination_day", lastDivinationDay);
        tag.putLong(prefix + "_current_cycle_index", currentCycleIndex);
        tag.putBoolean(prefix + "_san_zero_triggered", sanZeroTriggered);
        tag.putBoolean(prefix + "_forbidden_active", forbiddenActive);
        tag.putBoolean(prefix + "_abyss_shield", abyssShield);
        tag.putLong(prefix + "_last_left_water_time", lastLeftWaterTime);
        tag.putLong(prefix + "_last_sleep_restore_time", lastSleepRestoreTime);
        tag.putString(prefix + "_active_oracles", String.join(",", activeOracles));

        StringBuilder stars = new StringBuilder();
        for (Map.Entry<String, Integer> entry : oracleStars.entrySet()) {
            if (!stars.isEmpty()) {
                stars.append(",");
            }
            stars.append(entry.getKey()).append(":").append(entry.getValue());
        }
        tag.putString(prefix + "_oracle_stars", stars.toString());
    }

    public void load(CompoundTag tag, String prefix) {
        setSan(tag.getInt(prefix + "_san").orElse(100));
        oracleFragments = Math.max(0, tag.getInt(prefix + "_oracle_fragments").orElse(0));
        oracleCountThisCycle = Math.max(0, tag.getInt(prefix + "_oracle_count_this_cycle")
                .or(() -> tag.getInt(prefix + "_blood_moon_cycle_oracle_count"))
                .orElse(0));
        lastDivinationGameTime = tag.getLong(prefix + "_last_divination_game_time").orElse(-100L);
        lastDivinationDay = tag.getLong(prefix + "_last_divination_day").orElse(-1L);
        currentCycleIndex = tag.getLong(prefix + "_current_cycle_index").orElse(-1L);
        sanZeroTriggered = tag.getBoolean(prefix + "_san_zero_triggered").orElse(false);
        forbiddenActive = tag.getBoolean(prefix + "_forbidden_active").orElse(false);
        abyssShield = tag.getBoolean(prefix + "_abyss_shield").orElse(false);
        lastLeftWaterTime = tag.getLong(prefix + "_last_left_water_time").orElse(-1L);
        lastSleepRestoreTime = tag.getLong(prefix + "_last_sleep_restore_time").orElse(-1L);

        activeOracles.clear();
        String activeRaw = tag.getString(prefix + "_active_oracles").orElse("");
        if (!activeRaw.isBlank()) {
            for (String oracleId : activeRaw.split(",")) {
                if (OracleManager.getOracle(oracleId) != null) {
                    activeOracles.add(oracleId);
                }
            }
        }

        oracleStars.clear();
        String starsRaw = tag.getString(prefix + "_oracle_stars").orElse("");
        if (!starsRaw.isBlank()) {
            for (String token : starsRaw.split(",")) {
                String[] parts = token.split(":");
                if (parts.length == 2) {
                    try {
                        if (OracleManager.getOracle(parts[0]) != null) {
                            int parsed = Integer.parseInt(parts[1]);
                            if (parsed > 0) {
                                oracleStars.put(parts[0], Math.min(OracleManager.MAX_ORACLE_STAR, parsed));
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        activeOracles.retainAll(oracleStars.keySet());
        activeOracles.addAll(oracleStars.keySet());
    }
}
