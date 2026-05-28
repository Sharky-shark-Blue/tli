# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

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
The previous `AGENTS.md` focused on workflow behavior; keep these principles when editing code:
- Think before coding: state assumptions and ask when ambiguous.
- Prefer minimal solutions: avoid speculative abstractions/features.
- Make surgical changes only: touch lines directly required by the request.
- Define verifiable success criteria (tests/task outcomes) before implementing.
