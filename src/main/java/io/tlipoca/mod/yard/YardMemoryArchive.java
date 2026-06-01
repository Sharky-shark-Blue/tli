package io.tlipoca.mod.yard;

public final class YardMemoryArchive {
    private static final int[] STAGE_THRESHOLDS = {0, 3, 8, 15, 24};

    private YardMemoryArchive() {
    }

    public static int stageIndex(int archivedFragments) {
        int stage = 0;
        for (int i = 0; i < STAGE_THRESHOLDS.length; i++) {
            if (archivedFragments >= STAGE_THRESHOLDS[i]) {
                stage = i;
            }
        }
        return stage;
    }

    public static String stageName(int archivedFragments) {
        return switch (stageIndex(archivedFragments)) {
            case 0 -> "空页";
            case 1 -> "初次归档";
            case 2 -> "被记住";
            case 3 -> "旧幕重排";
            default -> "边境索引";
        };
    }

    public static String stageText(int archivedFragments) {
        return switch (stageIndex(archivedFragments)) {
            case 0 -> "账簿里还没有能留下重量的记忆。";
            case 1 -> "有些碎片开始认得这本账簿。";
            case 2 -> "名字之间出现了可以回头的路。";
            case 3 -> "旧剧场的座位表正在重新排列。";
            default -> "庭院开始把你也写进边境。";
        };
    }

    public static String nextHint(int archivedFragments) {
        for (int threshold : STAGE_THRESHOLDS) {
            if (archivedFragments < threshold) {
                return "下一阶段：" + archivedFragments + " / " + threshold;
            }
        }
        return "归档已抵达当前版本的尽头。";
    }
}
