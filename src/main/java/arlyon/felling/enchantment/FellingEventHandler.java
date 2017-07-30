package arlyon.felling.enchantment;

import arlyon.felling.Configuration;
import arlyon.felling.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Random;

/**
 * Handles the destroying of trees with the felling enchantment.
 */
public class FellingEventHandler {

    private enum TreePart {
        LEAF,
        LOG,
    }

    private EnumFacing[] fellingOneDirections = {EnumFacing.UP, EnumFacing.DOWN};
    private EnumFacing[] fellingTwoDirections = {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};

    /**
     * Determines if, given the config files, that the block should be cut down
     *
     * @param blockType the block to test
     * @return a boolean value indicating whether the block should be destroyed
     */
    private boolean shouldBreak(TreePart blockType) {
        return Configuration.cutLeaves ? blockType == TreePart.LOG || blockType == TreePart.LEAF : blockType == TreePart.LOG;
    }

    /**
     * Determines whether a block is a log, a leaf, or neither.
     * <p>
     * - makes sure the "block" isn't just air
     * - gets the id of logWood/treeLeaves in the OreDict
     * - returns whether it's a log or a leaf
     *
     * @param block the block to identify
     * @return the part of the tree it is, or alternatively null
     */
    private TreePart getTreePart(Block block) {
        if (block == Blocks.AIR) {
            return null;
        } else {
            int logID = OreDictionary.getOreID("logWood");
            int leafID = OreDictionary.getOreID("treeLeaves");
            int[] blockIDs = OreDictionary.getOreIDs(new ItemStack(block, 1));

            for (int id : blockIDs) {
                if (id == logID) {
                    return TreePart.LOG;
                } else if (id == leafID) {
                    return TreePart.LEAF;
                }
            }

            return null;
        }
    }

    /**
     * Intercepts the block break event to inject the felling enchantment logic.
     *
     * @param event the block break event that is called each time a minecraft block is broken.
     */
    @SubscribeEvent
    public void fellTreeSubscriber(BlockEvent.BreakEvent event) {

        EntityPlayer thePlayer = event.getPlayer();
        ItemStack mainHandItem = thePlayer.getHeldItemMainhand();
        int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Constants.felling, mainHandItem);

        // ignore anything without the enchantment
        if (enchantmentLevel == 0) {
            return;
        }

        IBlockState blockState = event.getState();
        Block block = blockState.getBlock();
        TreePart treePart = getTreePart(block);

        // if it isn't a log, then end
        if (!shouldBreak(treePart)) {
            return;
        }

        // sneaking players break blocks normally
        if (thePlayer.isSneaking()) {
            return;
        }

        World world = event.getWorld();
        BlockPos blockPosition = event.getPos();

        fellingAlgorithm(blockState, blockPosition, world, mainHandItem, thePlayer, treePart, enchantmentLevel == 1 ? fellingOneDirections : fellingTwoDirections);
    }

    /**
     * Attempts to break the block and returns false if the tool breaks during the operation.
     *
     * @param blockState
     * @param blockPosition
     * @param world
     * @param mainHandItem
     * @param thePlayer
     * @return
     */
    private boolean tryBreakBlock(IBlockState blockState, BlockPos blockPosition, World world, ItemStack mainHandItem, EntityPlayer thePlayer, TreePart treePart) {

        // delete the block
        world.setBlockToAir(blockPosition);

        // if in creative, we are done
        if (thePlayer.capabilities.isCreativeMode) {
            return true;
        }

        // drop the block
        blockState.getBlock().dropBlockAsItem(world, blockPosition, blockState, 0);

        // damage the tool, and if it is broken, return false (which signifies we should halt operation)
        if (mainHandItem.attemptDamageItem(treePart == TreePart.LEAF ? Configuration.durabilityDamage * Configuration.leafMultiplier / 100 : Configuration.durabilityDamage, new Random())) {
            thePlayer.inventory.deleteStack(mainHandItem);
            return false;
        }

        return true;
    }

    private void fellingAlgorithm(IBlockState blockState, BlockPos blockPosition, World world, ItemStack mainHandItem, EntityPlayer thePlayer, TreePart treePart, EnumFacing[] directions) {
        // try to break the block and if it fails then return
        if (!tryBreakBlock(blockState, blockPosition, world, mainHandItem, thePlayer, treePart)) {
            return;
        }

        // for each of the cardinal directions (NSEW + UD) poll for a log and recursively try and break it
        for (EnumFacing direction : directions) {
            BlockPos nextBlockPosition = blockPosition.offset(direction);
            IBlockState nextBlockState = world.getBlockState(nextBlockPosition);
            TreePart nextTreePart = getTreePart(nextBlockState.getBlock());

            // if the block in the specified direction relative to the parent block is a log, then cut it down too!
            if (shouldBreak(nextTreePart)) {
                fellingAlgorithm(nextBlockState, nextBlockPosition, world, mainHandItem, thePlayer, nextTreePart, directions);
            }
        }
    }
}
