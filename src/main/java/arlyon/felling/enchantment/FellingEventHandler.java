package arlyon.felling.enchantment;

import arlyon.felling.Constants;
import jdk.nashorn.internal.ir.BlockStatement;
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

import java.util.Random;

/**
 * Handles the destroying of trees with the felling enchantment.
 */
public class FellingEventHandler {
    /**
     * Intercepts the block break event to inject the felling enchantment logic.
     * @param event
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

        // sneaking players break blocks normally
        if (thePlayer.isSneaking()) {
            return;
        }

        IBlockState blockState = event.getState();
        Block block = blockState.getBlock();

        // break anything but wood normally
        if (!(block == Blocks.LOG || block == Blocks.LOG2)) {
            return;
        }

        World world = event.getWorld();
        BlockPos blockPosition = event.getPos();

        // if enchant
        if (enchantmentLevel == 1) {
            fellTreeSimple(blockState, blockPosition, world, mainHandItem, thePlayer);
        }

        // enchantment level 2
        if (enchantmentLevel == 2) {
            fellTreeAdvanced(blockState, blockPosition, world, mainHandItem, thePlayer);
        }
    }

    /**
     * Attempts to break the block and returns false if the tool breaks during the operation.
     * @param blockState
     * @param blockPosition
     * @param world
     * @param mainHandItem
     * @param thePlayer
     * @return
     */
    private boolean attemptBreakBlock(IBlockState blockState, BlockPos blockPosition, World world, ItemStack mainHandItem, EntityPlayer thePlayer) {

        // delete and drop the block
        blockState.getBlock().dropBlockAsItem(world, blockPosition, blockState, 0);
        world.setBlockToAir(blockPosition);

        // disable damage in creative
        if (thePlayer.capabilities.isCreativeMode) {
            return true;
        }

        // damage the tool and check for a broken tool
        if (mainHandItem.attemptDamageItem(4, new Random())) {
            thePlayer.inventory.deleteStack(mainHandItem);
            return false;
        }

        return true;
    }

    /**
     * Travels up the tree and breaks blocks until a) no more logs, b) no more axe
     * @param blockState
     * @param blockPosition
     * @param world
     * @param mainHandItem
     * @param thePlayer
     */
    private void fellTreeSimple(IBlockState blockState, BlockPos blockPosition, World world, ItemStack mainHandItem, EntityPlayer thePlayer) {

        // try to break the block and return if failed
        if (!attemptBreakBlock(blockState, blockPosition, world, mainHandItem, thePlayer)) {
            return;
        }

        // get block above
        BlockPos blockPositionAbove = blockPosition.offset(EnumFacing.UP);
        IBlockState blockStateAbove = world.getBlockState(blockPositionAbove);

        // if its a log, smash that bitch!
        if (blockStateAbove.getBlock() == Blocks.LOG || blockStateAbove.getBlock() == Blocks.LOG2) {
            fellTreeSimple(blockStateAbove, blockPositionAbove, world, mainHandItem, thePlayer);
        }
    }

    /**
     * Cuts down all the logs in every direction around the selected log.
     * @param blockState
     * @param blockPosition
     * @param world
     * @param mainHandItem
     * @param thePlayer
     */
    private void fellTreeAdvanced(IBlockState blockState, BlockPos blockPosition, World world, ItemStack mainHandItem, EntityPlayer thePlayer) {

        // try to break the block and return if failed
        if (!attemptBreakBlock(blockState, blockPosition, world, mainHandItem, thePlayer)) {
            return;
        }

        // destroy block up
        relativeFellTreeAdvanced(blockPosition, world, mainHandItem, thePlayer, EnumFacing.UP);

        // destroy block down
        relativeFellTreeAdvanced(blockPosition, world, mainHandItem, thePlayer, EnumFacing.DOWN);

        // destroy block north
        relativeFellTreeAdvanced(blockPosition, world, mainHandItem, thePlayer, EnumFacing.NORTH);

        // destroy block south
        relativeFellTreeAdvanced(blockPosition, world, mainHandItem, thePlayer, EnumFacing.SOUTH);

        // destroy block east
        relativeFellTreeAdvanced(blockPosition, world, mainHandItem, thePlayer, EnumFacing.EAST);

        // destroy block west
        relativeFellTreeAdvanced(blockPosition, world, mainHandItem, thePlayer, EnumFacing.WEST);
    }

    /**
     * Calls the fellTreeAdvanced function in the direction you specify relative to a given blockPosition.
     * @param blockPosition
     * @param world
     * @param mainHandItem
     * @param thePlayer
     * @param direction
     */
    private void relativeFellTreeAdvanced(
            BlockPos blockPosition,
            World world, ItemStack mainHandItem,
            EntityPlayer thePlayer, EnumFacing direction) {

        BlockPos nextBlockPosition = blockPosition.offset(direction);
        IBlockState nextBlockState = world.getBlockState(nextBlockPosition);

        if (nextBlockState.getBlock() == Blocks.LOG || nextBlockState.getBlock() == Blocks.LOG2) {
            fellTreeAdvanced(nextBlockState, nextBlockPosition, world, mainHandItem, thePlayer);
        }
    }
}
