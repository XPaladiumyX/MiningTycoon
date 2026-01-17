package skyxnetwork.miningTycoon.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlockPlaceListener implements Listener {

    private final MiningTycoon plugin;
    private final Set<Material> protectedBlocks;

    public BlockPlaceListener(MiningTycoon plugin) {
        this.plugin = plugin;
        this.protectedBlocks = new HashSet<>(Arrays.asList(
                Material.BLACK_WOOL, Material.STONE_SLAB, Material.SHORT_GRASS, Material.TALL_GRASS,
                Material.GRASS_BLOCK, Material.DIRT, Material.SPRUCE_PLANKS, Material.SPRUCE_SLAB,
                Material.SPRUCE_STAIRS, Material.SPRUCE_FENCE, Material.OAK_TRAPDOOR, Material.ENDER_CHEST,
                Material.SPRUCE_SIGN, Material.OAK_SIGN, Material.CHEST, Material.SPRUCE_FENCE_GATE,
                Material.WHITE_WOOL, Material.RED_WOOL, Material.ORANGE_WOOL, Material.PURPLE_WOOL,
                Material.PINK_WOOL, Material.BLUE_WOOL, Material.CYAN_WOOL, Material.GRAY_WOOL,
                Material.LIGHT_GRAY_WOOL, Material.BROWN_WOOL, Material.GREEN_WOOL, Material.LIGHT_BLUE_WOOL,
                Material.LIME_WOOL, Material.YELLOW_WOOL, Material.MAGENTA_WOOL, Material.ANVIL,
                Material.CHAIN, Material.GLOWSTONE, Material.BLACK_STAINED_GLASS, Material.BARRIER,
                Material.GRAY_CONCRETE, Material.DARK_OAK_SLAB, Material.COBBLESTONE_WALL, Material.LANTERN,
                Material.TORCH, Material.OAK_LOG, Material.STONE_STAIRS, Material.SPRUCE_TRAPDOOR,
                Material.COARSE_DIRT, Material.PODZOL, Material.MOSSY_COBBLESTONE, Material.MOSS_BLOCK,
                Material.OAK_LEAVES, Material.CAVE_VINES, Material.POINTED_DRIPSTONE, Material.LARGE_AMETHYST_BUD,
                Material.PURPLE_STAINED_GLASS, Material.SMOOTH_QUARTZ_STAIRS, Material.QUARTZ_SLAB,
                Material.END_ROD, Material.PINK_STAINED_GLASS, Material.QUARTZ_BLOCK, Material.WHITE_CARPET,
                Material.SEA_LANTERN, Material.WHITE_STAINED_GLASS, Material.QUARTZ_PILLAR, Material.QUARTZ_BRICKS,
                Material.OAK_STAIRS, Material.GOLD_BLOCK, Material.COBWEB, Material.PLAYER_HEAD,
                Material.MANGROVE_LEAVES, Material.VINE, Material.ROOTED_DIRT, Material.MUD,
                Material.HANGING_ROOTS, Material.ACACIA_SLAB, Material.ACACIA_TRAPDOOR, Material.ACACIA_STAIRS,
                Material.STRIPPED_JUNGLE_WOOD, Material.HAY_BLOCK, Material.BIRCH_SLAB, Material.BIRCH_PLANKS,
                Material.BIRCH_STAIRS, Material.BIRCH_TRAPDOOR, Material.STRIPPED_BIRCH_LOG,
                Material.BLACK_TERRACOTTA, Material.BLACK_STAINED_GLASS_PANE, Material.COBBLESTONE_SLAB,
                Material.COBBLESTONE_STAIRS, Material.COBBLESTONE, Material.DIRT_PATH, Material.DARK_OAK_FENCE_GATE,
                Material.YELLOW_TERRACOTTA, Material.GRAY_STAINED_GLASS_PANE, Material.STONE_BUTTON,
                Material.OAK_BUTTON, Material.NETHER_BRICK_FENCE, Material.LEVER, Material.DARK_OAK_TRAPDOOR,
                Material.POLISHED_BLACKSTONE_BUTTON, Material.DAYLIGHT_DETECTOR, Material.GRAY_CARPET,
                Material.BLACK_CARPET, Material.YELLOW_CARPET, Material.BLUE_CARPET, Material.GREEN_CARPET,
                Material.LIGHT_BLUE_CARPET, Material.LAPIS_BLOCK, Material.SEA_PICKLE, Material.TUBE_CORAL_BLOCK,
                Material.BRAIN_CORAL_BLOCK, Material.FIRE_CORAL_BLOCK, Material.HORN_CORAL_BLOCK,
                Material.TUBE_CORAL, Material.FIRE_CORAL_FAN, Material.SAND, Material.TURTLE_EGG,
                Material.STRIPPED_SPRUCE_WOOD, Material.GLOW_LICHEN, Material.AZALEA_LEAVES, Material.STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS, Material.FLOWERING_AZALEA_LEAVES, Material.SPORE_BLOSSOM,
                Material.RED_TERRACOTTA, Material.CANDLE, Material.DEAD_HORN_CORAL_FAN, Material.ANDESITE_SLAB,
                Material.REDSTONE_BLOCK, Material.NETHER_WART_BLOCK, Material.REDSTONE_TORCH,
                Material.SHROOMLIGHT, Material.ANDESITE_STAIRS, Material.ANDESITE, Material.CRIMSON_ROOTS,
                Material.CHERRY_LEAVES, Material.RED_STAINED_GLASS, Material.ORANGE_STAINED_GLASS,
                Material.YELLOW_STAINED_GLASS, Material.GREEN_STAINED_GLASS, Material.BLUE_STAINED_GLASS,
                Material.BROWN_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS, Material.GRAY_STAINED_GLASS,
                Material.LIGHT_GRAY_STAINED_GLASS, Material.CHISELED_STONE_BRICKS, Material.STONE_BRICK_WALL,
                Material.LIME_STAINED_GLASS, Material.SLIME_BLOCK, Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
                Material.REDSTONE
        ));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        // Check if block is protected
        if (protectedBlocks.contains(blockType)) {
            // Allow only if player has bypass permission or is OP in creative
            if (!player.hasPermission("antiblock.bypass") &&
                    !(player.isOp() && player.getGameMode() == GameMode.CREATIVE)) {
                event.setCancelled(true);
                player.sendMessage("§cYou do not have permission to break this block!");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Only allow block placement if player has permission or is OP in creative
        if (!player.hasPermission("antiblock.bypass") &&
                !(player.isOp() && player.getGameMode() == GameMode.CREATIVE)) {
            event.setCancelled(true);
            player.sendMessage("§cYou do not have permission to place blocks!");
        }
    }
}
