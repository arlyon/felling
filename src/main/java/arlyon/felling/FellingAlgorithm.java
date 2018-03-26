package arlyon.felling;

import arlyon.felling.util.ValueUniqueQueue;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Random;

import static net.minecraft.init.Enchantments.FORTUNE;

/**
 * The main class for the felling algorithm.
 * Takes a world, position and player and destroys
 * all contiguous trees.
 */
public class FellingAlgorithm {

    /**
     * Used to determine which part of the tree a block is.
     */
    private enum TreePart {
        LEAF,
        LOG,
    }

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
     * Breaks the block at a given position and then for
     * each path continues felling on that block as well.
     *
     * @param blockPosition The position of the block to start on.
     * @param world    The world.
     * @param player   The player to start the algorithm.
     * @return Whether the algorithm executed.
     */
    public static boolean fellingAlgorithm(BlockPos blockPosition, World world, EntityPlayer player) {
        if (getTreePart(world, blockPosition) == null) return false; // if not a tree part return

        EnumFacing[][] paths = getPaths(player.getHeldItemMainhand());

        ValueUniqueQueue<BlockPos> blocksToBreak = new ValueUniqueQueue<>(value -> value <= Configuration.serverSide.maxDistance, Integer::compareTo);
        blocksToBreak.add(blockPosition, 0);
        int blocksBroken = 0;

        while (!blocksToBreak.isEmpty() && (blocksBroken <= Configuration.serverSide.maxBlocks || Configuration.serverSide.maxBlocks == 0)) {
            blockPosition = blocksToBreak.peek(); // next block
            int distance = blocksToBreak.getValue(blockPosition);
            blocksToBreak.remove();

            if (distance == 0) {
                breakBlock(blockPosition, world, player); // is a log, break
                blocksBroken += 1;
            }

            if (mainHandBreaksWhenDamaged(player, getTreePart(world, blockPosition))) return;

            for (EnumFacing[] pathToFollow : paths) {
                BlockPos nextBlockPosition = travelToBlock(blockPosition, pathToFollow);
                TreePart nextTreePart = getTreePart(world, nextBlockPosition);

                if (shouldBreakTreePart(nextTreePart)) {
                    blocksToBreak.add(nextBlockPosition, 0);
                } else {
                    blocksToBreak.add(nextBlockPosition, distance + 1);
                }
            }
        }

        return true;
    }

    /**
     * Gets the block type of the block.
     *
     * @return The block type.
     */
    private static TreePart getTreePart(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block.isWood(world, pos))
            return TreePart.LOG;
        else if (block.isLeaves(block.getDefaultState(), world, pos))
            return TreePart.LEAF;
        else
            return null;
    }

    /**
     * Gets the list of valid felling paths given an event.
     *
     * @param item The enchanted item.
     * @return The list of paths matching the felling enchantment version.
     */
    private static EnumFacing[][] getPaths(ItemStack item) {
        return fellingPaths[EnchantmentHelper.getEnchantmentLevel(Felling.felling, item) - 1];
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
    private static boolean mainHandBreaksWhenDamaged(EntityPlayer thePlayer, TreePart treePart) {
        if (thePlayer.isCreative()) return false;
        if (!toolBreaksWhenDamaged((EntityPlayerMP) thePlayer, thePlayer.getHeldItemMainhand(), treePart)) return false;

        thePlayer.inventory.deleteStack(thePlayer.getHeldItemMainhand());
        return true;
    }

    /**
     * Checks all the blocks reached from the list of paths and checks
     * if they are valid breaks before returning the lost of all valid.
     *
     * @param blocks          The block queue to add valid blocks to.
     * @param world           The world
     * @param paths           The array of paths to get to the valid blocks.
     * @param currentDistance The current distance.
     */
    private static void getSurroundingBlocks(ValueUniqueQueue<BlockPos> blocks, World world, EnumFacing[][] paths, int currentDistance) {

    }

    /**
     * Gets the server side config to see if leaf cutting is enabled
     * and determines if a given tree part should break.
     *
     * @param treePart The type of block being cut.
     * @return Whether the felling algorithm should run.
     */
    private static boolean shouldBreakTreePart(TreePart treePart) {
        return Configuration.serverSide.cutLeaves ? treePart == TreePart.LOG || treePart == TreePart.LEAF : treePart == TreePart.LOG;
    }

    /**
     * Damages the given item and returns whether it should break.
     *
     * @param thePlayer The player to deal damage to.
     * @param theTool   The tool to deal damage to.
     * @param treePart  The part of the tree.
     * @return Whether the tool breaks.
     */
    private static boolean toolBreaksWhenDamaged(EntityPlayerMP thePlayer, ItemStack theTool, TreePart treePart) {
        return theTool.attemptDamageItem(
                treePart == TreePart.LEAF ?
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
}
