# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project snapshot
- Forge mod scaffold targeting **Minecraft 1.21.10** with **Forge 60.1.0** and **Java 21**.
- Build system: **Gradle Wrapper** (`gradlew` / `gradlew.bat`) with ForgeGradle + Parchment mappings.
- Primary mod package: `io.tlipoca.mod`.

## Common commands
Run from repository root (`L:/MC`).

- List available tasks:
  - `./gradlew tasks --all`
- Build mod jar:
  - `./gradlew build`
- Clean build outputs:
  - `./gradlew clean`
- Run checks/tests:
  - `./gradlew check`
  - `./gradlew test`
- Run a single test class:
  - `./gradlew test --tests "io.tlipoca.mod.SomeTest"`
- Run a single test method:
  - `./gradlew test --tests "io.tlipoca.mod.SomeTest.someMethod"`
- Launch Minecraft client in dev mode:
  - `./gradlew runClient`
- Launch dedicated server in dev mode:
  - `./gradlew runServer`
- Run GameTest server:
  - `./gradlew runGameTestServer`
- Run data generation (outputs generated assets/resources):
  - `./gradlew runData`

Notes:
- Wrapper currently points to Gradle 8.12.1 (`gradle/wrapper/gradle-wrapper.properties`).
- ForgeGradle run configs use `run/` and `run-data/` working directories.

## High-level architecture

### 1) Entry point and registration flow
- `src/main/java/io/tlipoca/mod/TlipocaMod.java` is the mod entry point (`@Mod("tlipoca")`).
- The mod uses Forge `DeferredRegister` for:
  - Blocks
  - Items
  - Creative mode tabs
- In the `TlipocaMod` constructor, registers are attached to the mod event bus group, then lifecycle listeners are wired:
  - Common setup listener
  - Creative tab content listener
  - Config spec registration

### 2) Runtime event model
- Common lifecycle behavior lives in `TlipocaMod#commonSetup` (server+client shared initialization path).
- Client-only lifecycle hooks are isolated in `TlipocaMod.ClientModEvents` via `@Mod.EventBusSubscriber(..., value = Dist.CLIENT)`.
- Creative tab insertion is handled by subscribing to `BuildCreativeModeTabContentsEvent`.

### 3) Configuration system
- `src/main/java/io/tlipoca/mod/Config.java` defines mod config via `ForgeConfigSpec`.
- Pattern used:
  1. Define config entries statically in `ForgeConfigSpec.Builder`
  2. Build `SPEC`
  3. On `ModConfigEvent`, materialize values into static runtime fields
- Item config values are stored as resource-location strings, then resolved to `Item` instances through `ForgeRegistries.ITEMS`.

### 4) Resource + metadata pipeline
- `src/main/resources/META-INF/mods.toml` carries loader metadata and dependency constraints for Forge + Minecraft.
- `processResources` in `build.gradle` expands placeholders (`${mod_id}`, `${minecraft_version}`, etc.) from `gradle.properties` into `mods.toml` and `pack.mcmeta`.
- Generated resources are included through `src/generated/resources` (wired into main resources source set).

### 5) Version and identity control points
- `gradle.properties` is the single source of truth for:
  - Minecraft/Forge/mapping versions
  - Mod identity (`mod_id`, `mod_name`, group/version)
  - Loader and compatibility ranges
- Java toolchain is fixed to 21 in `build.gradle`.

## Existing behavioral rules in this repo
The previous `CLAUDE.md` focused on workflow behavior; keep these principles when editing code:
- Think before coding: state assumptions and ask when ambiguous.
- Prefer minimal solutions: avoid speculative abstractions/features.
- Make surgical changes only: touch lines directly required by the request.
- Define verifiable success criteria (tests/task outcomes) before implementing.

## 当前玩法实现进度

1. 占卜核心循环：右键占卜台 → 扣SAN → 获得/升格神谕 → 同日拒绝重复
2. SAN 数据：每人独立，持久化，死亡继承，重生同步
3. 命令系统：/tlipoca oracle get / setSan / clear
4. 联机同步：只给本人发包，客户端缓存 localSan + forbiddenActive
5. HUD：左上角常驻 SAN: x/100，forbidden_vitality 激活时 "SAN" 变红
6. 神谕效果（全部已验收）：
   - dagon_gold：水下 Haste II；离水 6000tick 后每 60tick 饱食度 -1
   - earth_vein_memory：挖矿概率触发附近矿脉粒子，SAN -1
   - hunter_gaze：击杀敌对生物（含投射物）概率攻击力+2，SAN -1
   - forbidden_vitality：SAN≤30 力量I，SAN≤15 加抗性提升I，激活时 HUD 变红
   - mist_step：夜晚露天速度I，每200tick幽魂粒子
   - abyss_echo：占卜后概率激活护盾，下次受伤归零，SAN -3
7. SAN=0 惩罚：失明15s + 随机传送30格 + SAN重置为15
8. 调试命令：/tlipoca debug setAll / setOracle / status / triggerOracle / resetAll / shield

## 庭院系统与资源实现进度

1. 庭院核心方块：
   - `yard_anchor`：庭院锚点，注册到 `YardManager`，玩家在 32 格范围内视为处于庭院。
   - `yard_alchemy_table`：庭院炼金台，要求玩家处于庭院范围内才可执行炼金。
   - `yard_offering_pedestal`：供物台，可放置/移除单个供物，并通过方块实体保存与同步。
2. 庭院炼金：
   - 炼金台扫描自身东南西北四个方向的供物台。
   - 匹配配方后消耗供物台上的供物，并在炼金台上方生成产物。
   - 已有基础配方：安神药、幻翼膜、发光墨囊。
   - 新增庭院风格配方链：
     - 蜂巢 + 发光浆果 + 紫水晶碎片 -> `star_honey`
     - 藤蔓 + 兰花 + 玻璃瓶 -> `moondew_leaf`
     - 墨囊 + 紫水晶碎片 + `moondew_leaf` -> `oracle_ink`
     - 纸 + 灵魂沙 + `oracle_ink` -> `soul_receipt`
     - 书 + `soul_receipt` + `star_honey` -> `guest_ledger`
3. 新增庭院物品：
   - `guest_ledger`：庭院账簿，3D 手持模型，作为庭院管理工具。
   - `soul_receipt`：灵魂收据。
   - `moondew_leaf`：月露叶。
   - `star_honey`：星蜜，可食用，轻量恢复。
   - `oracle_ink`：神谕墨水。
   - 这些物品使用 `YardLoreItem` 添加悬浮说明文本。
4. 庭院账簿功能：
   - 手持 `guest_ledger` 右键 `yard_anchor` 会扫描庭院半径内设施。
   - 输出锚点、炼金台、供物台数量。
   - 至少 1 个炼金台 + 3 个供物台时提示炼金布置可运转。
5. 方块模型进度：
   - `yard_anchor` 已从整块模型重置为低矮庭院界碑/锚点模型。
   - `yard_alchemy_table` 已从整块模型重置为四脚炼金工作台模型。
   - `yard_offering_pedestal` 已重置为带托盘的小供物台模型。
   - 这些非完整方块已在注册属性中加入 `noOcclusion()`，防止底部空隙透视到地底。
6. 物品/方块资源注意事项：
   - Minecraft 1.21.10 物品需要 `assets/tlipoca/items/*.json` 映射到 `models/item/*.json`，否则会出现紫黑缺失纹理。
   - 已为现有物品和方块物品补齐 `assets/tlipoca/items/` 下的映射文件。
   - `guest_ledger` 使用 `models/item/guest_ledger.json` + `textures/item/guest_ledger_3d.png` 实现 3D 手持模型。
7. Blockbench 工作流约定：
   - 已接入 MCP Blockbench，可直接创建项目、放置 cube、绘制贴图、导出模型。
   - 以后做模型时应把实际贴图导入/绑定到 Blockbench 预览，避免只用临时色块导致游戏内效果和预览偏差。

## 环境/构建注意事项

- `gradle.properties` 已设置 `systemProp.net.minecraftforge.gradle.check.certs=false`，用于绕过本机上 ForgeGradle 校验 `maven.minecraftforge.net` 证书失败的问题。
- `gradlew tasks --all` 与 `gradlew build` 已在该设置下验证可通过。

## 下一步候选
- SAN 自然衰减（让 SAN 成为需要主动管理的资源）
- 占卜台音效与粒子（仪式感）
- 新神谕或神祇系统扩展
- 庭院账簿 GUI（显示设施列表、炼金配方进度、庭院状态）
- 庭院方块粒子/音效（锚点范围提示、炼金成功反馈、供物台供物悬浮效果）
- 继续细化庭院贴图与 Blockbench 预览，统一为“优格索托斯的庭院”式经营+神秘日常风格
