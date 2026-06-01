package io.tlipoca.mod.yard;

import io.tlipoca.mod.TlipocaMod;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record MistVisitorRequest(
    String id,
    String title,
    String record,
    String requestText,
    String rewardText,
    Supplier<Item> requestItem,
    int requestCount,
    Supplier<Item> rewardItem,
    int rewardCount,
    String missingMessage,
    String successMessage
) {
    private static final List<MistVisitorRequest> REQUESTS = List.of(
        new MistVisitorRequest(
            "lost_audience",
            "迷路的观众",
            "他说自己只是来晚了。",
            "褪色邀请函 x1",
            "记忆碎片 x1",
            TlipocaMod.FADED_INVITATION,
            1,
            TlipocaMod.MEMORY_FRAGMENT,
            1,
            "门外的人还在等一张邀请函。",
            "邀请函被收走了。雾里落下一片记忆。"
        ),
        new MistVisitorRequest(
            "nameless_guest",
            "没有名字的人",
            "名字被雾盖住了。",
            "星蜜 x1",
            "旧剧票 x1",
            TlipocaMod.STAR_HONEY,
            1,
            TlipocaMod.OLD_THEATER_TICKET,
            1,
            "门外的人还在等一点星蜜。",
            "门口只剩下一张旧剧票。"
        ),
        new MistVisitorRequest(
            "wet_messenger",
            "湿掉的信使",
            "信封里没有地址。",
            "雾露 x1",
            "神谕墨水 x1",
            TlipocaMod.FOG_DEW,
            1,
            TlipocaMod.ORACLE_INK,
            1,
            "信使还在等一滴雾露。",
            "湿信封里渗出一点墨。"
        ),
        new MistVisitorRequest(
            "empty_seat",
            "门外的空座",
            "座位被留给了没人。",
            "旧剧票 x1",
            "无名花 x1",
            TlipocaMod.OLD_THEATER_TICKET,
            1,
            TlipocaMod.NAMELESS_FLOWER_ITEM,
            1,
            "空座还在等一张旧剧票。",
            "座位空了，那里长出一朵无名花。"
        )
    );

    public static MistVisitorRequest currentFor(ServerPlayer player) {
        long day = player.level().getGameTime() / 24000L;
        int seed = player.getUUID().hashCode();
        int index = Math.floorMod((int) (day * 31L + seed), REQUESTS.size());
        return REQUESTS.get(index);
    }

    public ItemStack createRewardStack() {
        return new ItemStack(rewardItem.get(), rewardCount);
    }
}
