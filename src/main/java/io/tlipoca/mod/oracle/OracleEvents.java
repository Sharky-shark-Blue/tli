package io.tlipoca.mod.oracle;

import com.mojang.logging.LogUtils;
import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.network.TlipocaNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Result;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = TlipocaMod.MODID)
public final class OracleEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    private OracleEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerOracleData data = OracleManager.getData(player);
            OracleManager.saveData(player, data);
            OracleManager.resetSanZeroStateIfRecovered(player);
            OracleManager.syncSan(player);
        }
    }

    @SubscribeEvent
    public static void onRightClickDivinationTable(PlayerInteractEvent.RightClickBlock event) {
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OracleManager.resetSanZeroStateIfRecovered(player);
            OracleManager.syncSan(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer) || !(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }
        PlayerOracleData oldData = OracleManager.getData(oldPlayer);
        OracleManager.saveData(newPlayer, oldData);
        OracleManager.syncSan(newPlayer);
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.wakeImmediately() || event.updateLevel()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            OracleManager.restoreSanFromSleep(player);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        PlayerOracleData data = OracleManager.getData(player);
        BlockState state = event.getState();
        BlockPos pos = event.getPos();

        boolean saveData = false;

        if (OracleManager.applyDagonGold(player, data)) {
            saveData = true;
            LOGGER.info("oracle_effect dagon_gold player={} pos={}", player.getName().getString(), pos);
        }

        if (isOre(state) && OracleManager.triggerEarthVeinMemory(player, level, data, pos, state)) {
            saveData = false;
        }

        if (saveData) {
            OracleManager.saveData(player, data);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getEntity().getType().getCategory() != MobCategory.MONSTER) {
            return;
        }

        ServerPlayer player = resolveKillerPlayer(event.getSource().getEntity(), event.getSource().getDirectEntity());
        if (player == null) {
            return;
        }

        PlayerOracleData data = OracleManager.getData(player);
        if (OracleManager.triggerHunterGaze(player, data, player.level().getGameTime())) {
            player.displayClientMessage(Component.literal("猎者凝视令你的杀意沸腾。"), true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PlayerOracleData data = OracleManager.getData(player);
        if (!data.isAbyssShield()) {
            return;
        }

        event.setAmount(0.0F);
        data.setAbyssShield(false);
        if (!OracleManager.isCreativeDebugPlayer(player)) {
            data.consumeSan(3);
        }
        if (data.getSan() == 0) {
            OracleManager.triggerSanZeroEffect(player, data);
        }
        OracleManager.saveData(player, data);
        OracleManager.syncSan(player);
        LOGGER.info("oracle_effect abyss_echo player={} san={}", player.getName().getString(), data.getSan());
    }

    private static ServerPlayer resolveKillerPlayer(Entity sourceEntity, Entity directEntity) {
        if (sourceEntity instanceof ServerPlayer player) {
            return player;
        }
        if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (event.side() != LogicalSide.SERVER || !(event.player() instanceof ServerPlayer player)) {
            return;
        }

        PlayerOracleData data = OracleManager.getData(player);
        long gameTime = player.level().getGameTime();
        boolean saveData = false;
        boolean syncSan = false;

        if (OracleManager.isCreativeDebugPlayer(player) && data.getSan() != 100) {
            data.setSan(100);
            saveData = true;
            syncSan = true;
        }

        if (OracleManager.tickDagonGold(player, data, gameTime)) {
            saveData = true;
        }
        if (OracleManager.tickNaturalSanDecay(player, data, gameTime)) {
            saveData = true;
            syncSan = true;
        }
        if (OracleManager.tickSunlightSanRestore(player, data, gameTime)) {
            saveData = true;
            syncSan = true;
        }
        OracleManager.tickDagonGoldPenalty(player, data, gameTime);
        OracleManager.tickEarthVeinMarkers(player);
        OracleManager.tickHunterGaze(player, gameTime);

        if (gameTime % 40 == 0 && OracleManager.updateForbiddenVitality(player, data)) {
            saveData = true;
            syncSan = true;
        }

        OracleManager.tickMistStep(player, gameTime);

        if (saveData) {
            OracleManager.saveData(player, data);
        }
        if (syncSan) {
            OracleManager.syncSan(player);
        }
    }

    private static boolean isOre(BlockState state) {
        return state.getBlock().builtInRegistryHolder().key().location().getPath().contains("_ore");
    }
}
