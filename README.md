# Tlipoca

这是一个基于 Forge 1.21.10 / Java 21 的《犹格索托斯的庭院》同人 Minecraft 模组。

## 模组介绍

特莉波卡因仪式事故穿越到 Minecraft 世界。她不是 Boss，也不是敌人，而是玩家身边的见习死神同伴。

当前版本围绕 **SAN、神谕、庭院据点、庭院炼金、收魂记录、雾夜来信与悬赏** 展开。庭院不是重经营系统，而是玩家用于准备、炼金、恢复、查看记录、回应来信和接取收魂悬赏的异界基地。

## 当前主要内容

### SAN 与神谕

- 每名玩家独立 SAN 数据，支持持久化、死亡继承、重生同步。
- 左上角常驻 HUD：`SAN: x/100`。
- SAN 自然衰减与基础回复系统。
- SAN=0 惩罚：失明、随机传送、SAN 重置为 15。
- 6 个神谕效果：
  - `dagon_gold`：水下急迫 II；离水 6000 tick 后饱食度缓慢流失。
  - `earth_vein_memory`：挖矿概率标记附近同类矿石，SAN -1。
  - `hunter_gaze`：击杀敌对生物概率攻击力 +2，SAN -1。
  - `forbidden_vitality`：SAN≤30 力量 I，SAN≤15 抗性提升 I，激活时 HUD “SAN” 变红。
  - `mist_step`：夜晚露天速度 I，并生成幽魂粒子。
  - `abyss_echo`：占卜后概率获得一次性伤害抵消护盾，触发时 SAN -3。
- 神谕典藏 GUI：Shift + 右键占卜台打开。

### 庭院系统

- `yard_anchor` / 庭院锚点：定义 32 格庭院范围。
- `yard_alchemy_table` / 庭院炼金台。
- `yard_offering_pedestal` / 供物台：右键放入供物，左键取回，供物悬浮显示。
- `guest_ledger` / 庭院账簿：
  - 显示庭院连接状态、SAN、已解锁神谕数量。
  - 显示庭院氛围：舒适度、异界度、记忆度。
  - 显示庭院阶段：陌生、被注意、被邀请、稳定边境。
  - 显示已知庭院炼金配方。
  - 显示收割记录与雾夜来信记录。

### 庭院素材与炼金

- 新增庭院素材：
  - `faded_invitation` / 褪色邀请函
  - `memory_fragment` / 记忆碎片
  - `fog_dew` / 雾露
  - `old_theater_ticket` / 旧剧票
  - `moondew_leaf` / 月露叶
  - `star_honey` / 星蜜
  - `oracle_ink` / 神谕墨水
  - `soul_receipt` / 灵魂收据
  - `soul_calming_draft` / 安神药
- 新增庭院装饰：
  - `mist_lamp` / 雾灯
  - `old_poster` / 旧海报
  - `nameless_flower` / 无名花
- 庭院炼金配方链已接入账簿显示。

### 雾夜来信

- 当玩家在庭院内、主世界夜晚、庭院阶段至少达到“被邀请”时，账簿显示雾夜来信。
- 当前来信请求：
  - 请求：蜂蜜瓶 x2
  - 回礼：记忆碎片 x1
- 每名玩家每个 Minecraft 游戏日只能回应一次。
- 账簿记录累计回应来信数量。

### 收魂、悬赏与战斗

- `trainee_reaper_scythe` / 见习死神的残镰：
  - 击杀敌对生物收容灵魂。
  - 在庭院中释放后获得灵魂收据。
  - 账簿记录累计处理灵魂数量，并永久保留“……对不起。”
- `bounty_board` / 悬赏板 与 `bounty_contract` / 悬赏单：
  - 支持悬赏刷新、领取、完成检测。
  - 显示中文目标名和时限。
  - 包含低概率特殊悬赏“村外徘徊者”。
- `soul_calming_draft` / 安神药：
  - 饮用后 SAN +15。
  - 生存模式喝完返还玻璃瓶。

## 开发环境

- Minecraft 1.21.10
- Forge 60.1.0
- Java 21
- Mod ID: `tlipoca`
- 主包名：`io.tlipoca.mod`

## 构建

```powershell
.\gradlew.bat clean compileJava --console=plain
.\gradlew.bat build
```

构建产物位于：

```text
build/libs/tlipoca-1.0.0.jar
```
