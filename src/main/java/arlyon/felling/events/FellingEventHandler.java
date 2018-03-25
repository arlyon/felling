package arlyon.felling.events;

import arlyon.felling.Configuration;
import arlyon.felling.Felling;
import arlyon.felling.network.PlayerSettings;
import arlyon.felling.support.ValueUniqueQueue;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

import static net.minecraft.init.Enchantments.FORTUNE;

public class FellingEventHandler {

    /**
     * The paths for felling one, to reach up and down.
     */
    private static final EnumFacing[][] firstTierPaths = {
            // up and down
            {EnumFacing.UP},
            {EnumFacing.DOWN}
    };
    /**
     * The paths for felling two, to reach all adjacent blocks.
     */
    private static final EnumFacing[][] secondTierPaths = {
            // adjacent
            firstTierPaths[0],
            firstTierPaths[1],
            {EnumFacing.NORTH},
            {EnumFacing.SOUTH},
            {EnumFacing.EAST},
            {EnumFacing.WEST}
    };
    /**
     * The paths for felling three, to reach adjacent blocks including diagonals.
     */
    private static final EnumFacing[][] thirdTierPaths = {
            // adjacent
            secondTierPaths[0],
            secondTierPaths[1],
            secondTierPaths[2],
            secondTierPaths[3],
            secondTierPaths[4],
            secondTierPaths[5],
            // corners
            {EnumFacing.NORTH, EnumFacing.WEST},
            {EnumFacing.NORTH, EnumFacing.EAST},
            {EnumFacing.SOUTH, EnumFacing.WEST},
            {EnumFacing.SOUTH, EnumFacing.EAST},
            // top/bottom corners
            {EnumFacing.UP, EnumFacing.NORTH, EnumFacing.WEST},
            {EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST},
            {EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.WEST},
            {EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.EAST},
            {EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.WEST},
            {EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.EAST},
            {EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.WEST},
            {EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.EAST},
    };
    /**
     * Array of all the paths for each of the three felling levels.
     */
    private static final EnumFacing[][][] fellingPaths = {
            firstTierPaths,
            secondTierPaths,
            thirdTierPaths,
    };

    /**
     * Listens to block break events and checks to see if
     * we need to start the felling algorithm and runs it.
     *
     * @param event The break event.
     */
    @SubscribeEvent
    public void fellingBlockBreakSubscriber(BlockEvent.BreakEvent event) {
        if (shouldStartFelling(event)) startFelling(event);
    }

    /**
     * Makes some checks to see if it is a valid felling event.
     *
     * @param currentEvent The break event.
     * @return Whether the felling should run.
     */
    private boolean shouldStartFelling(BlockEvent.BreakEvent currentEvent) {
        return (configAllowsBreak(currentEvent) &&
                eventIsServerSide(currentEvent) &&
                mainHandHasEnchantment(currentEvent) &&
                treePartShouldBreak(getTreePart(currentEvent.getState().getBlock())));
    }

    /**
     * Calls the felling algorithm with the needed data from the event.
     *
     * @param currentEvent The block break event.
     */
    private void startFelling(BlockEvent.BreakEvent currentEvent) {
        fellingAlgorithm(
                currentEvent.getPos(),
                currentEvent.getWorld(),
                currentEvent.getPlayer(),
                getTreePart(currentEvent.getState().getBlock()),
                getPaths(currentEvent));
    }

    /**
     * Checks the configs to see if the block should break by checking
     * if the player is standing or crouching and testing if the config
     * has the given state enabled or disabled.
     *
     * @param event The block break event.
     * @return Whether the config allows the break.
     */
    private boolean configAllowsBreak(BlockEvent.BreakEvent event) {
        PlayerSettings playerSettings = Felling.getOrCreatePlayerSettings(event.getPlayer());
        return event.getPlayer().isSneaking() ? !playerSettings.disableWhenCrouched : !playerSettings.disableWhenStanding;
    }

    /**
     * Checks if the event is server-side (mainly for readability).
     *
     * @param event The break event.
     * @return Whether the event is being called on the server side.
     */
    private boolean eventIsServerSide(BlockEvent.BreakEvent event) {
        return !event.getWorld().isRemote; // remote compared to the server
    }

    /**
     * A simple check to see if the player that caused the break event has the enchantment in their main hand.
     *
     * @param event The block break event.
     * @return Whether the player's main hand is enchanted.
     */
    private boolean mainHandHasEnchantment(BlockEvent.BreakEvent event) {
        return EnchantmentHelper.getEnchantmentLevel(Felling.felling, event.getPlayer().getHeldItemMainhand()) != 0;
    }

    /**
     * Gets the server side config to see if leaf cutting is enabled
     * and determines if a given tree part should break.
     *
     * @param blockType The type of block being cut.
     * @return Whether the felling algorithm should run.
     */
    private static boolean treePartShouldBreak(BlockType blockType) {
        return Configuration.serverSide.cutLeaves ? blockType == BlockType.LOG || blockType == BlockType.LEAF : blockType == BlockType.LOG;
    }

    /**
     * Gets the block type of the block.
     *
     * @param block The block to test.
     * @return The block type.
     */
    private static BlockType getTreePart(Block block) {
        if (block.isWood(null, null))
            return BlockType.LOG;
        else if (block.isLeaves(block.getDefaultState(), null, null))
            return BlockType.LEAF;
        else
            return null;
    }

    /**
     * Breaks the block at a given position and then for
     * each path continues felling on that block as well.
     *
     * @param blockPosition The position of the block to start on.
     * @param world         The world.
     * @param thePlayer     The player.
     * @param treePart      The tree part.
     * @param paths         The paths to fell.
     */
    private static void fellingAlgorithm(BlockPos blockPosition, World world, EntityPlayer thePlayer, BlockType treePart, EnumFacing[][] paths) {
        BlockPos currentPosition = blockPosition;

        ValueUniqueQueue<BlockPos> blocks = new ValueUniqueQueue<>(value -> value <= Configuration.serverSide.maxDistance, Integer::compareTo);
        blocks.add(currentPosition, 0);
        int blocksBroken = 0;

        while (!blocks.isEmpty() && (blocksBroken <= Configuration.serverSide.maxBlocks || Configuration.serverSide.maxBlocks == 0)) {
            currentPosition = blocks.peek(); // next block
            int distance = blocks.getValue(currentPosition);

            if (distance == 0) {
                breakBlock(currentPosition, world, thePlayer); // is a log, break
                blocksBroken += 1;
            }

            if (mainHandBreaksWhenDamaged(thePlayer, treePart)) return;

            getSurroundingBlocks(blocks, world, paths, distance);
        }
    }

    /**
     * Gets the list of valid felling paths given an event.
     *
     * @param event The break event.
     * @return The list of paths matching the felling enchantment version.
     */
    private static EnumFacing[][] getPaths(BlockEvent.BreakEvent event) {
        return fellingPaths[EnchantmentHelper.getEnchantmentLevel(Felling.felling, event.getPlayer().getHeldItemMainhand()) - 1];
    }

    /**
     * Given a position, sets the block to air and drops the item.
     *
     * @param blockPosition The position.
     * @param world         The world.
     * @param thePlayer     The player.
     */
    private static void breakBlock(BlockPos blockPosition, World world, EntityPlayer thePlayer) {
        if (!thePlayer.capabilities.isCreativeMode) {
            int fortune = EnchantmentHelper.getEnchantmentLevel(FORTUNE, thePlayer.getHeldItemMainhand());

            world.getBlockState(blockPosition).getBlock().dropBlockAsItem(world, blockPosition, world.getBlockState(blockPosition), fortune); // drop the block
            world.getBlockState(blockPosition).getBlock().dropXpOnBlockBreak( // drop the xp
                    world,
                    blockPosition,
                    world.getBlockState(blockPosition).getBlock().getExpDrop(
                            world.getBlockState(blockPosition),
                            world,
                            blockPosition,
                            fortune
                    )
            );
        }

        world.setBlockToAir(blockPosition); // delete the block
    }

    /**
     * Deals damage to the enchant and breaks if needed.
     *
     * @param thePlayer The player.
     * @param treePart  The part being broken.
     * @return Whether the tool was broken.
     */
    private static boolean mainHandBreaksWhenDamaged(EntityPlayer thePlayer, BlockType treePart) {
        if (thePlayer.isCreative()) return false;
        if (!toolBreaksWhenDamaged((EntityPlayerMP) thePlayer, thePlayer.getHeldItemMainhand(), treePart)) return false;

        thePlayer.inventory.deleteStack(thePlayer.getHeldItemMainhand());
        return true;
    }

    /**
     * Checks all the blocks reached from the list of paths and checks
     * if they are valid breaks before returning the lost of all valid.
     * @param blocks The block queue to add valid blocks to.
     * @param world The world
     * @param paths The array of paths to get to the valid blocks.
     * @param currentDistance The current distance.
     */
    private static void getSurroundingBlocks(ValueUniqueQueue<BlockPos> blocks, World world, EnumFacing[][] paths, int currentDistance) {
        BlockPos startPosition = blocks.remove();

        for (EnumFacing[] pathToFollow : paths) {
            BlockPos nextBlockPosition = travelToBlock(startPosition, pathToFollow);
            BlockType nextTreePart = getTreePart(world.getBlockState(nextBlockPosition).getBlock());

            if (treePartShouldBreak(nextTreePart)) {
                blocks.add(nextBlockPosition, 0);
            } else {
                blocks.add(nextBlockPosition, currentDistance + 1);
            }
        }
    }

    /**
     * Damages the given item and returns whether it should break.
     *
     * @param thePlayer The player to deal damage to.
     * @param theTool   The tool to deal damage to.
     * @param treePart  The part of the tree.
     * @return Whether the tool breaks.
     */
    private static boolean toolBreaksWhenDamaged(EntityPlayerMP thePlayer, ItemStack theTool, BlockType treePart) {
        return theTool.attemptDamageItem(
                treePart == BlockType.LEAF ?
                        Configuration.serverSide.durabilityDamage * Configuration.serverSide.leafMultiplier / 100 :
                        Configuration.serverSide.durabilityDamage,
                new Random(),
                thePlayer);
    }

    /**
     * Given a starting block and path of EnumFacings, follows the path and returns a new position.
     *
     * @param startingBlock The block to start on.
     * @param pathToFollow  The path to follow.
     * @return The new block position.
     */
    private static BlockPos travelToBlock(BlockPos startingBlock, EnumFacing[] pathToFollow) {
        return Arrays.stream(pathToFollow).reduce(startingBlock, BlockPos::offset, (a, b) -> null);
    }

    /**
     * Used to determine which part of the tree a block is.
     */
    private enum BlockType {
        LEAF,
        LOG,
    }
}