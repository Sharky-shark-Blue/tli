package io.tlipoca.mod.oracle;

import io.tlipoca.mod.TlipocaMod;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = TlipocaMod.MODID)
public final class OracleCommands {
    private static final ResourceLocation DEBUG_HUNTER_GAZE_ID = ResourceLocation.fromNamespaceAndPath(TlipocaMod.MODID, "debug_hunter_gaze");

    private OracleCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("tlipoca")
                        .then(Commands.literal("oracle")
                                .then(Commands.literal("get").executes(OracleCommands::runGet))
                                .then(Commands.literal("list").executes(OracleCommands::runList))
                                .then(Commands.literal("debug")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.literal("dumpNbt").executes(OracleCommands::runDumpNbt))
                                        .then(Commands.literal("setSan")
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                                        .executes(OracleCommands::runSetSan)))
                                        .then(Commands.literal("clear").executes(OracleCommands::runClear))
                                        .then(Commands.literal("divine")
                                                .executes(OracleCommands::runDivine)
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 20))
                                                        .executes(OracleCommands::runDivine)))
                                        .then(Commands.literal("grant")
                                                .then(Commands.argument("oracle_id", StringArgumentType.word())
                                                        .executes(OracleCommands::runGrant)
                                                        .then(Commands.argument("star", IntegerArgumentType.integer(1, OracleManager.MAX_ORACLE_STAR))
                                                                .executes(OracleCommands::runGrant))))
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("oracle_id", StringArgumentType.word())
                                                        .executes(OracleCommands::runRemove)))
                                        .then(Commands.literal("resetDaily").executes(OracleCommands::runResetDaily))
                                        .then(Commands.literal("setLastDay")
                                                .then(Commands.argument("day", IntegerArgumentType.integer(-1))
                                                        .executes(OracleCommands::runSetLastDay)))
                                        .then(Commands.literal("setCycleCount")
                                                .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                                        .executes(OracleCommands::runSetCycleCount)))
                                        .then(Commands.literal("setCycle")
                                                .then(Commands.argument("cycle", IntegerArgumentType.integer(-1))
                                                        .executes(OracleCommands::runSetCycle)))
                                        .then(Commands.literal("debugSetAll")
                                                .then(Commands.argument("star", IntegerArgumentType.integer(1, OracleManager.MAX_ORACLE_STAR))
                                                        .executes(OracleCommands::runDebugSetAll)))
                                )
                                .then(Commands.literal("dumpNbt")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(OracleCommands::runDumpNbt))
                                .then(Commands.literal("setSan")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                                .executes(OracleCommands::runSetSan)))
                                .then(Commands.literal("clear")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(OracleCommands::runClear))
                        )
                        .then(Commands.literal("debug")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("setAll")
                                        .then(Commands.argument("star", IntegerArgumentType.integer(1, OracleManager.MAX_ORACLE_STAR))
                                                .executes(OracleCommands::runDebugSetAll)))
                                .then(Commands.literal("setOracle")
                                        .then(Commands.argument("oracle_id", StringArgumentType.word())
                                                .then(Commands.argument("star", IntegerArgumentType.integer(1, OracleManager.MAX_ORACLE_STAR))
                                                        .executes(OracleCommands::runDebugSetOracle))))
                                .then(Commands.literal("status").executes(OracleCommands::runDebugStatus))
                                .then(Commands.literal("triggerOracle")
                                        .then(Commands.argument("oracle_id", StringArgumentType.word())
                                                .executes(OracleCommands::runDebugTriggerOracle)))
                                .then(Commands.literal("resetAll").executes(OracleCommands::runDebugResetAll))
                                .then(Commands.literal("shield").executes(OracleCommands::runDebugShield))
                        )
        );
    }

    private static int runGet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerOracleData data = OracleManager.ensureCreativeSan(player);
        long day = OracleManager.getCurrentDay(player);
        long cycle = OracleManager.getCycleIndex(day);
        String mode = OracleManager.isCreativeDebugPlayer(player) ? "creative_debug" : "survival";
        context.getSource().sendSuccess(
                () -> Component.literal(
                        "mode=" + mode
                                + ", SAN=" + data.getSan()
                                + ", day=" + day
                                + ", cycle=" + cycle
                                + ", lastDivinationDay=" + data.getLastDivinationDay()
                                + ", oracleCountThisCycle=" + data.getOracleCountThisCycle()
                                + ", ordinaryOracles=" + data.getActiveOracles()
                                + ", oracleStars=" + data.getOracleStars()
                ),
                false
        );
        return 1;
    }

    private static int runList(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
                () -> Component.literal("ordinaryOraclePool=" + OracleManager.getAvailableOracles(null).stream()
                        .map(definition -> definition.getId() + "(" + definition.getDisplayName() + ")")
                        .toList()),
                false
        );
        return 1;
    }

    private static int runDumpNbt(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerOracleData data = OracleManager.ensureCreativeSan(player);
        context.getSource().sendSuccess(
                () -> Component.literal(
                        "oracle_nbt player=" + player.getName().getString()
                                + ", uuid=" + player.getUUID()
                                + ", SAN=" + data.getSan()
                                + ", activeOracles=" + data.getActiveOracles()
                                + ", oracleStars=" + data.getOracleStars()
                                + ", oracleFragments=" + data.getOracleFragments()
                                + ", oracleCountThisCycle=" + data.getOracleCountThisCycle()
                                + ", lastDivinationDay=" + data.getLastDivinationDay()
                                + ", lastDivinationGameTime=" + data.getLastDivinationGameTime()
                                + ", currentCycleIndex=" + data.getCurrentCycleIndex()
                                + ", sanZeroTriggered=" + data.isSanZeroTriggered()
                ),
                false
        );
        return 1;
    }

    private static int runSetSan(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int value = IntegerArgumentType.getInteger(context, "value");
        PlayerOracleData data = OracleManager.getData(player);
        if (OracleManager.isCreativeDebugPlayer(player)) {
            data.setSan(100);
            OracleManager.saveData(player, data);
            OracleManager.syncSan(player);
            context.getSource().sendSuccess(() -> Component.literal("创造模式下 SAN 固定为 100。"), false);
            return 1;
        }
        data.setSan(value);
        OracleManager.saveData(player, data);
        OracleManager.resetSanZeroStateIfRecovered(player);
        OracleManager.syncSan(player);
        context.getSource().sendSuccess(() -> Component.literal("Set SAN to " + data.getSan()), false);
        return 1;
    }

    private static int runClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerOracleData data = new PlayerOracleData();
        if (OracleManager.isCreativeDebugPlayer(player)) {
            data.setSan(100);
        }
        OracleManager.saveData(player, data);
        OracleManager.syncSan(player);
        context.getSource().sendSuccess(() -> Component.literal("Oracle data cleared."), false);
        return 1;
    }

    private static int runDivine(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int count;
        try {
            count = IntegerArgumentType.getInteger(context, "count");
        } catch (IllegalArgumentException ignored) {
            count = 1;
        }
        for (int i = 0; i < count; i++) {
            OracleManager.tryDivine(player);
        }
        int executedCount = count;
        context.getSource().sendSuccess(() -> Component.literal("Debug divine executed " + executedCount + " time(s)."), false);
        return count;
    }

    private static int runGrant(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String oracleId = StringArgumentType.getString(context, "oracle_id");
        OracleDefinition definition = OracleManager.getOracle(oracleId);
        if (definition == null) {
            context.getSource().sendFailure(Component.literal("Unknown oracle: " + oracleId));
            return 0;
        }
        int star;
        try {
            star = IntegerArgumentType.getInteger(context, "star");
        } catch (IllegalArgumentException ignored) {
            star = 1;
        }
        PlayerOracleData data = OracleManager.ensureCreativeSan(player);
        data.setStar(oracleId, star);
        OracleManager.saveData(player, data);
        OracleManager.syncSan(player);
        int grantedStar = star;
        context.getSource().sendSuccess(() -> Component.literal("Granted oracle " + definition.getDisplayName() + " " + OracleManager.romanStar(grantedStar)), false);
        return 1;
    }

    private static int runRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String oracleId = StringArgumentType.getString(context, "oracle_id");
        PlayerOracleData data = OracleManager.ensureCreativeSan(player);
        data.removeOracle(oracleId);
        OracleManager.saveData(player, data);
        OracleManager.syncSan(player);
        context.getSource().sendSuccess(() -> Component.literal("Removed oracle " + oracleId), false);
        return 1;
    }

    private static int runResetDaily(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerOracleData data = OracleManager.ensureCreativeSan(player);
        data.setLastDivinationDay(-1);
        data.setLastDivinationGameTime(-100);
        OracleManager.saveData(player, data);
        context.getSource().sendSuccess(() -> Component.literal("Daily divination state reset."), false);
        return 1;
    }

    private static int runSetLastDay(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int day = IntegerArgumentType.getInteger(context, "day");
        PlayerOracleData data = OracleManager.ensureCreativeSan(player);
        data.setLastDivinationDay(day);
        OracleManager.saveData(player, data);
        context.getSource().sendSuccess(() -> Component.literal("Set lastDivinationDay to " + day), false);
        return 1;
    }

    private static int runSetCycleCount(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int count = IntegerArgumentType.getInteger(context, "count");
        PlayerOracleData data = OracleManager.ensureCreativeSan(player);
        data.setOracleCountThisCycle(count);
        OracleManager.saveData(player, data);
        context.getSource().sendSuccess(() -> Component.literal("Set oracleCountThisCycle to " + data.getOracleCountThisCycle()), false);
        return 1;
    }

    private static int runSetCycle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int cycle = IntegerArgumentType.getInteger(context, "cycle");
        PlayerOracleData data = OracleManager.ensureCreativeSan(player);
        data.setCurrentCycleIndex(cycle);
        OracleManager.saveData(player, data);
        context.getSource().sendSuccess(() -> Component.literal("Set currentCycleIndex to " + cycle), false);
        return 1;
    }

    private static int runDebugSetAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int star = IntegerArgumentType.getInteger(context, "star");
        PlayerOracleData data = OracleManager.ensureCreativeSan(player);

        for (OracleDefinition definition : OracleManager.getAvailableOracles(null)) {
            data.setStar(definition.getId(), star);
        }

        OracleManager.saveData(player, data);
        OracleManager.syncSan(player);

        player.sendSystemMessage(Component.literal(
                "[调试] dagon_gold=" + OracleManager.getOracleStar(data, "dagon_gold")
                        + ", earth_vein_memory=" + OracleManager.getOracleStar(data, "earth_vein_memory")
                        + ", hunter_gaze=" + OracleManager.getOracleStar(data, "hunter_gaze")
                        + ", forbidden_vitality=" + OracleManager.getOracleStar(data, "forbidden_vitality")
                        + ", mist_step=" + OracleManager.getOracleStar(data, "mist_step")
                        + ", abyss_echo=" + OracleManager.getOracleStar(data, "abyss_echo")
        ));
        return 1;
    }

    private static int runDebugSetOracle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String oracleId = StringArgumentType.getString(context, "oracle_id");
        int star = IntegerArgumentType.getInteger(context, "star");
        if (OracleManager.getOracle(oracleId) == null) {
            context.getSource().sendFailure(Component.literal("Unknown oracle: " + oracleId));
            return 0;
        }

        PlayerOracleData data = OracleManager.ensureCreativeSan(player);
        data.setStar(oracleId, star);
        OracleManager.saveData(player, data);
        OracleManager.syncSan(player);
        player.sendSystemMessage(Component.literal("[调试] " + oracleId + " 设为 " + OracleManager.getOracleStar(data, oracleId) + " 星"));
        return 1;
    }

    private static int runDebugStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerOracleData data = OracleManager.getData(player);
        player.sendSystemMessage(Component.literal("[SAN] 当前值: " + data.getSan()));
        player.sendSystemMessage(Component.literal(
                "[神谕] dagon_gold=" + OracleManager.getOracleStar(data, "dagon_gold")
                        + " earth_vein_memory=" + OracleManager.getOracleStar(data, "earth_vein_memory")
                        + " hunter_gaze=" + OracleManager.getOracleStar(data, "hunter_gaze")
                        + " forbidden_vitality=" + OracleManager.getOracleStar(data, "forbidden_vitality")
                        + " mist_step=" + OracleManager.getOracleStar(data, "mist_step")
                        + " abyss_echo=" + OracleManager.getOracleStar(data, "abyss_echo")
        ));
        player.sendSystemMessage(Component.literal(
                "[标记] abyssShield=" + data.isAbyssShield()
                        + " forbiddenActive=" + data.isForbiddenActive()
                        + " lastLeftWaterTime=" + data.getLastLeftWaterTime() + "（gameTime）"
        ));
        return 1;
    }

    private static int runDebugTriggerOracle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String oracleId = StringArgumentType.getString(context, "oracle_id");
        if (OracleManager.getOracle(oracleId) == null) {
            context.getSource().sendFailure(Component.literal("Unknown oracle: " + oracleId));
            return 0;
        }

        PlayerOracleData data = OracleManager.getData(player);
        switch (oracleId) {
            case "dagon_gold" -> player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 1, true, false));
            case "earth_vein_memory" -> triggerEarthVeinDebug(player);
            case "hunter_gaze" -> triggerHunterGazeDebug(player);
            case "forbidden_vitality" -> {
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false));
                data.setForbiddenActive(true);
                OracleManager.saveData(player, data);
                OracleManager.syncSan(player);
            }
            case "mist_step" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0, true, false));
                if (player.level() instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY(), player.getZ(), 8, 4.0D, 0.6D, 4.0D, 0.01D);
                }
            }
            case "abyss_echo" -> {
                data.setAbyssShield(true);
                OracleManager.saveData(player, data);
                OracleManager.syncSan(player);
            }
            default -> {
                return 0;
            }
        }
        player.sendSystemMessage(Component.literal("[调试] 强制触发 " + oracleId));
        return 1;
    }

    private static int runDebugResetAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerOracleData data = OracleManager.getData(player);
        data.clearOrdinaryOracles();
        data.setSan(100);
        data.setAbyssShield(false);
        data.setForbiddenActive(false);
        data.setLastLeftWaterTime(-1L);
        data.setOracleCountThisCycle(0);
        data.setLastDivinationDay(-1L);
        data.setLastDivinationGameTime(-100L);
        data.setSanZeroTriggered(false);
        OracleManager.saveData(player, data);
        OracleManager.syncSan(player);
        player.sendSystemMessage(Component.literal("[调试] 已重置"));
        return 1;
    }

    private static int runDebugShield(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerOracleData data = OracleManager.getData(player);
        data.setAbyssShield(true);
        OracleManager.saveData(player, data);
        OracleManager.syncSan(player);
        player.sendSystemMessage(Component.literal("[调试] 护盾已激活"));
        return 1;
    }

    private static void triggerEarthVeinDebug(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        List<BlockPos> orePositions = new ArrayList<>();
        BlockPos origin = player.blockPosition();
        for (int offsetX = -OracleManager.EARTH_VEIN_RADIUS; offsetX <= OracleManager.EARTH_VEIN_RADIUS; offsetX++) {
            for (int offsetY = -OracleManager.EARTH_VEIN_RADIUS; offsetY <= OracleManager.EARTH_VEIN_RADIUS; offsetY++) {
                for (int offsetZ = -OracleManager.EARTH_VEIN_RADIUS; offsetZ <= OracleManager.EARTH_VEIN_RADIUS; offsetZ++) {
                    BlockPos targetPos = origin.offset(offsetX, offsetY, offsetZ);
                    BlockState state = level.getBlockState(targetPos);
                    if (state.getBlock().builtInRegistryHolder().key().location().getPath().contains("_ore")) {
                        orePositions.add(targetPos.immutable());
                    }
                }
            }
        }
        for (BlockPos orePos : orePositions) {
            level.sendParticles(ParticleTypes.END_ROD, orePos.getX() + 0.5D, orePos.getY() + 0.5D, orePos.getZ() + 0.5D, 5, 0.2D, 0.2D, 0.2D, 0.01D);
        }
    }

    private static void triggerHunterGazeDebug(ServerPlayer player) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }
        attackDamage.addOrUpdateTransientModifier(new AttributeModifier(DEBUG_HUNTER_GAZE_ID, 2.0D, AttributeModifier.Operation.ADD_VALUE));
    }
}
