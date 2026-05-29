package io.tlipoca.mod;

import com.mojang.logging.LogUtils;
import io.tlipoca.mod.block.BountyBoardBlock;
import io.tlipoca.mod.block.DivinationTableBlock;
import io.tlipoca.mod.block.YardAlchemyTableBlock;
import io.tlipoca.mod.block.YardAnchorBlock;
import io.tlipoca.mod.block.YardOfferingPedestalBlock;
import io.tlipoca.mod.block.entity.BountyBoardBlockEntity;
import io.tlipoca.mod.block.entity.YardAnchorBlockEntity;
import io.tlipoca.mod.block.entity.YardOfferingPedestalBlockEntity;
import io.tlipoca.mod.item.BountyContractItem;
import io.tlipoca.mod.item.SoulCalmingDraftItem;
import io.tlipoca.mod.item.TraineeReaperScytheItem;
import io.tlipoca.mod.item.YardLedgerItem;
import io.tlipoca.mod.item.YardLoreItem;
import io.tlipoca.mod.network.TlipocaNetwork;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TlipocaMod.MODID)
public final class TlipocaMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tlipoca";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
        () -> new Block(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("example_block"))
            .mapColor(MapColor.STONE)
        )
    );

    public static final RegistryObject<Block> DIVINATION_TABLE = BLOCKS.register("divination_table",
        () -> new DivinationTableBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("divination_table"))
            .mapColor(MapColor.COLOR_PURPLE)
            .noOcclusion()
            .strength(2.5F, 6.0F)
        )
    );

    public static final RegistryObject<Block> YARD_ANCHOR = BLOCKS.register("yard_anchor",
        () -> new YardAnchorBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("yard_anchor"))
            .mapColor(MapColor.COLOR_PURPLE)
            .noOcclusion()
            .strength(3.0F, 6.0F)
        )
    );

    public static final RegistryObject<Block> YARD_ALCHEMY_TABLE = BLOCKS.register("yard_alchemy_table",
        () -> new YardAlchemyTableBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("yard_alchemy_table"))
            .mapColor(MapColor.COLOR_PURPLE)
            .noOcclusion()
            .strength(2.8F, 6.0F)
        )
    );

    public static final RegistryObject<Block> YARD_OFFERING_PEDESTAL = BLOCKS.register("yard_offering_pedestal",
        () -> new YardOfferingPedestalBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("yard_offering_pedestal"))
            .mapColor(MapColor.COLOR_PURPLE)
            .noOcclusion()
            .strength(2.0F, 6.0F)
        )
    );

    public static final RegistryObject<Block> BOUNTY_BOARD = BLOCKS.register("bounty_board",
        () -> new BountyBoardBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("bounty_board"))
            .mapColor(MapColor.WOOD)
            .strength(2.0F, 4.0F)
        )
    );

    public static final RegistryObject<Block> MIST_LAMP = BLOCKS.register("mist_lamp",
        () -> new Block(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("mist_lamp"))
            .mapColor(MapColor.COLOR_PURPLE)
            .lightLevel(state -> 12)
            .strength(1.0F, 3.0F)
        )
    );

    public static final RegistryObject<Block> OLD_POSTER = BLOCKS.register("old_poster",
        () -> new Block(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("old_poster"))
            .mapColor(MapColor.WOOD)
            .strength(0.4F, 1.0F)
        )
    );

    public static final RegistryObject<Block> NAMELESS_FLOWER = BLOCKS.register("nameless_flower",
        () -> new Block(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("nameless_flower"))
            .mapColor(MapColor.PLANT)
            .noOcclusion()
            .strength(0.2F, 0.2F)
        )
    );
    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
        () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties().setId(ITEMS.key("example_block")))
    );

    public static final RegistryObject<Item> DIVINATION_TABLE_ITEM = ITEMS.register("divination_table",
        () -> new BlockItem(DIVINATION_TABLE.get(), new Item.Properties().setId(ITEMS.key("divination_table")))
    );

    public static final RegistryObject<Item> YARD_ANCHOR_ITEM = ITEMS.register("yard_anchor",
        () -> new BlockItem(YARD_ANCHOR.get(), new Item.Properties().setId(ITEMS.key("yard_anchor")))
    );

    public static final RegistryObject<Item> YARD_ALCHEMY_TABLE_ITEM = ITEMS.register("yard_alchemy_table",
        () -> new BlockItem(YARD_ALCHEMY_TABLE.get(), new Item.Properties().setId(ITEMS.key("yard_alchemy_table")))
    );

    public static final RegistryObject<Item> YARD_OFFERING_PEDESTAL_ITEM = ITEMS.register("yard_offering_pedestal",
        () -> new BlockItem(YARD_OFFERING_PEDESTAL.get(), new Item.Properties().setId(ITEMS.key("yard_offering_pedestal")))
    );

    public static final RegistryObject<Item> BOUNTY_BOARD_ITEM = ITEMS.register("bounty_board",
        () -> new BlockItem(BOUNTY_BOARD.get(), new Item.Properties().setId(ITEMS.key("bounty_board")))
    );

    public static final RegistryObject<Item> MIST_LAMP_ITEM = ITEMS.register("mist_lamp",
        () -> new BlockItem(MIST_LAMP.get(), new Item.Properties().setId(ITEMS.key("mist_lamp")))
    );

    public static final RegistryObject<Item> OLD_POSTER_ITEM = ITEMS.register("old_poster",
        () -> new BlockItem(OLD_POSTER.get(), new Item.Properties().setId(ITEMS.key("old_poster")))
    );

    public static final RegistryObject<Item> NAMELESS_FLOWER_ITEM = ITEMS.register("nameless_flower",
        () -> new BlockItem(NAMELESS_FLOWER.get(), new Item.Properties().setId(ITEMS.key("nameless_flower")))
    );

    public static final RegistryObject<Item> GUEST_LEDGER = ITEMS.register("guest_ledger",
        () -> new YardLedgerItem(new Item.Properties().setId(ITEMS.key("guest_ledger")), "item.tlipoca.guest_ledger.desc")
    );

    public static final RegistryObject<Item> FADED_INVITATION = ITEMS.register("faded_invitation",
        () -> new YardLoreItem(new Item.Properties().setId(ITEMS.key("faded_invitation")), "item.tlipoca.faded_invitation.desc")
    );

    public static final RegistryObject<Item> MEMORY_FRAGMENT = ITEMS.register("memory_fragment",
        () -> new YardLoreItem(new Item.Properties().setId(ITEMS.key("memory_fragment")), "item.tlipoca.memory_fragment.desc")
    );

    public static final RegistryObject<Item> FOG_DEW = ITEMS.register("fog_dew",
        () -> new YardLoreItem(new Item.Properties().setId(ITEMS.key("fog_dew")).stacksTo(16), "item.tlipoca.fog_dew.desc")
    );

    public static final RegistryObject<Item> OLD_THEATER_TICKET = ITEMS.register("old_theater_ticket",
        () -> new YardLoreItem(new Item.Properties().setId(ITEMS.key("old_theater_ticket")), "item.tlipoca.old_theater_ticket.desc")
    );

    public static final RegistryObject<Item> SOUL_RECEIPT = ITEMS.register("soul_receipt",
        () -> new YardLoreItem(new Item.Properties().setId(ITEMS.key("soul_receipt")), "item.tlipoca.soul_receipt.desc")
    );

    public static final RegistryObject<Item> MOONDEW_LEAF = ITEMS.register("moondew_leaf",
        () -> new YardLoreItem(new Item.Properties().setId(ITEMS.key("moondew_leaf")), "item.tlipoca.moondew_leaf.desc")
    );

    public static final RegistryObject<Item> STAR_HONEY = ITEMS.register("star_honey",
        () -> new YardLoreItem(new Item.Properties()
            .setId(ITEMS.key("star_honey"))
            .food(new FoodProperties.Builder()
                .nutrition(2)
                .saturationModifier(0.4F)
                .build()
            ),
            "item.tlipoca.star_honey.desc"
        )
    );

    public static final RegistryObject<Item> ORACLE_INK = ITEMS.register("oracle_ink",
        () -> new YardLoreItem(new Item.Properties().setId(ITEMS.key("oracle_ink")), "item.tlipoca.oracle_ink.desc")
    );

    public static final RegistryObject<Item> SOUL_CALMING_DRAFT = ITEMS.register("soul_calming_draft",
        () -> new SoulCalmingDraftItem(new Item.Properties().setId(ITEMS.key("soul_calming_draft")).stacksTo(16))
    );

    public static final RegistryObject<Item> BOUNTY_CONTRACT = ITEMS.register("bounty_contract",
        () -> new BountyContractItem(new Item.Properties().setId(ITEMS.key("bounty_contract")).stacksTo(1))
    );

    public static final RegistryObject<Item> TRAINEE_REAPER_SCYTHE = ITEMS.register("trainee_reaper_scythe",
        () -> new TraineeReaperScytheItem(new Item.Properties()
            .setId(ITEMS.key("trainee_reaper_scythe"))
            .sword(ToolMaterial.IRON, 3.0F, -2.8F)
            .durability(384)
            .rarity(Rarity.UNCOMMON)
            .stacksTo(1))
    );

    public static final RegistryObject<BlockEntityType<YardAnchorBlockEntity>> YARD_ANCHOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("yard_anchor",
        () -> new BlockEntityType<>(YardAnchorBlockEntity::new, Set.of(YARD_ANCHOR.get()))
    );

    public static final RegistryObject<BlockEntityType<YardOfferingPedestalBlockEntity>> YARD_OFFERING_PEDESTAL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("yard_offering_pedestal",
        () -> new BlockEntityType<>(YardOfferingPedestalBlockEntity::new, Set.of(YARD_OFFERING_PEDESTAL.get()))
    );

    public static final RegistryObject<BlockEntityType<BountyBoardBlockEntity>> BOUNTY_BOARD_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("bounty_board",
        () -> new BlockEntityType<>(BountyBoardBlockEntity::new, Set.of(BOUNTY_BOARD.get()))
    );

    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item",
        () -> new Item(new Item.Properties()
            .setId(ITEMS.key("example_item"))
            .food(new FoodProperties.Builder()
                .alwaysEdible()
                .nutrition(1)
                .saturationModifier(2f)
                .build()
            )
        )
    );

    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    public TlipocaMod(FMLJavaModLoadingContext context) {
        LOGGER.info("TLIPOCA LOADED: TlipocaMod constructor called");
        var modBusGroup = context.getModBusGroup();

        // Register the commonSetup method for modloading
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modBusGroup);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modBusGroup);
        BLOCK_ENTITY_TYPES.register(modBusGroup);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modBusGroup);

        // Register the item to a creative tab
        BuildCreativeModeTabContentsEvent.BUS.addListener(TlipocaMod::addCreative);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            net.minecraftforge.client.event.AddGuiOverlayLayersEvent.BUS.addListener(io.tlipoca.mod.client.SanHudRenderer::register);
        }

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        TlipocaNetwork.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add block items to the mod's creative tab
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CREATIVE_MODE_TABS.key("example_tab"))) {
            event.accept(DIVINATION_TABLE_ITEM);
            event.accept(YARD_ANCHOR_ITEM);
            event.accept(YARD_ALCHEMY_TABLE_ITEM);
            event.accept(YARD_OFFERING_PEDESTAL_ITEM);
            event.accept(BOUNTY_BOARD_ITEM);
            event.accept(MIST_LAMP_ITEM);
            event.accept(OLD_POSTER_ITEM);
            event.accept(NAMELESS_FLOWER_ITEM);
            event.accept(GUEST_LEDGER);
            event.accept(FADED_INVITATION);
            event.accept(MEMORY_FRAGMENT);
            event.accept(FOG_DEW);
            event.accept(OLD_THEATER_TICKET);
            event.accept(SOUL_RECEIPT);
            event.accept(MOONDEW_LEAF);
            event.accept(STAR_HONEY);
            event.accept(ORACLE_INK);
            event.accept(SOUL_CALMING_DRAFT);
            event.accept(BOUNTY_CONTRACT);
            event.accept(TRAINEE_REAPER_SCYTHE);
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(YARD_OFFERING_PEDESTAL_BLOCK_ENTITY.get(), io.tlipoca.mod.client.YardOfferingPedestalRenderer::new);
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
