package arlyon.felling.events;

import arlyon.felling.Configuration;
import arlyon.felling.Constants;
import arlyon.felling.packets.PlayerSettings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Map;
import java.util.Random;

public class EnchantmentEventHandler {

    /**
     * Used to determine which part of the tree a block is.
     */
    private enum BlockType {
        LEAF,
        LOG,
    }

    /**
     * The paths for felling one, to reach up and down.
     */
    private static EnumFacing[][] firstTierPaths = {
            // up and down
            {EnumFacing.UP},
            {EnumFacing.DOWN}
    };

    /**
     * The paths for felling two, to reach all adjacent blocks.
     */
    private static EnumFacing[][] secondTierPaths = {
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
    private static EnumFacing[][] thirdTierPaths = {
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
    private static EnumFacing[][][] fellingPaths = {
            firstTierPaths,
            secondTierPaths,
            thirdTierPaths,
    };

    @SubscribeEvent
    public void fellingBlockBreakSubscriber(BlockEvent.BreakEvent event) {
        if (shouldStartFelling(event)) startFelling(event);
    }

    private boolean shouldStartFelling(BlockEvent.BreakEvent currentEvent) {
        return (configAllowsBreak(currentEvent) && // the config is set correctly
                eventIsServerside(currentEvent) && // the instance is server-side
                mainHandHasEnchantment(currentEvent) && // the weapon has the enchantment
                treePartShouldBreak(getTreePart(currentEvent.getState().getBlock()))); // the block should break
    }

    private void startFelling(BlockEvent.BreakEvent currentEvent) {
        fellingAlgorithm(
                currentEvent.getState(),
                currentEvent.getPos(),
                currentEvent.getWorld(),
                currentEvent.getPlayer(),
                getTreePart(currentEvent.getState().getBlock()),
                getPaths(currentEvent));
    }

    private static BlockType getTreePart(Block block) {
        return getTreePart(new ItemStack(block, 1));
    }

    private static BlockType getTreePart(ItemStack stack) {
        if (stack.isEmpty()) return null;

        int[] blockIDs = OreDictionary.getOreIDs(stack);

        int logID = OreDictionary.getOreID("logWood");
        int leafID = OreDictionary.getOreID("treeLeaves");

        for (int id : blockIDs) {
            if (id == logID) return BlockType.LOG;
            if (id == leafID) return BlockType.LEAF;
        }

        return registerIncompatibleBlock(stack) ? getTreePart(stack) : null;
    }

    private static boolean registerIncompatibleBlock(ItemStack stack) {
        if (!stack.getUnlocalizedName().matches("^ic2.rubber_wood$")) return false;

        OreDictionary.registerOre("logWood", stack);
        return true;
    }

    private boolean configAllowsBreak(BlockEvent.BreakEvent event) {

        PlayerSettings playerSettings = getOrCreatePlayerSettings(event.getPlayer());
        return (event.getPlayer().isSneaking() && !playerSettings.disableWhenCrouched) || (!event.getPlayer().isSneaking() && !playerSettings.disableWhenStanding);

    }

    private PlayerSettings getOrCreatePlayerSettings(EntityPlayer thePlayer) {
        PlayerSettings playerSettings = Constants.playerSettings.get(thePlayer.getGameProfile().hashCode());

        if (playerSettings == null) {
            playerSettings = new PlayerSettings(true, true);
            thePlayer.sendMessage(new TextComponentString("Your Felling settings aren't synced with the server. Please update the settings in the mod config to resend them."));
        }

        return playerSettings;
    }

    private boolean eventIsServerside(BlockEvent.BreakEvent event) {
        return !event.getWorld().isRemote; // remote compared to the server
    }

    private boolean mainHandHasEnchantment(BlockEvent.BreakEvent event) {
        return EnchantmentHelper.getEnchantmentLevel(Constants.felling, event.getPlayer().getHeldItemMainhand()) != 0;
    }

    private static boolean treePartShouldBreak(BlockType blockType) {
        return Configuration.serverSide.cutLeaves ? blockType == BlockType.LOG || blockType == BlockType.LEAF : blockType == BlockType.LOG;
    }

    private static EnumFacing[][] getPaths(BlockEvent.BreakEvent event) {
        return fellingPaths[EnchantmentHelper.getEnchantmentLevel(Constants.felling, event.getPlayer().getHeldItemMainhand()) - 1];
    }

    private static void fellingAlgorithm(IBlockState blockState, BlockPos blockPosition, World world, EntityPlayer thePlayer, BlockType treePart, EnumFacing[][] paths) {
        // try to break the block and if it fails then return

        breakBlock(blockState, blockPosition, world, thePlayer, treePart);
        if (mainHandBreaksWhenDamaged(thePlayer, treePart)) return;

        // for each path passed in, travel to the block and test it
        for (EnumFacing[] path : paths) {
            continueFelling(blockPosition, world, thePlayer, paths, path);
        }
    }

    private static void continueFelling(BlockPos currentBlockPosition, World world, EntityPlayer thePlayer, EnumFacing[][] paths, EnumFacing[] pathToFollow) {

        BlockPos nextBlockPosition = travelToBlock(currentBlockPosition, pathToFollow);
        IBlockState nextBlockState = world.getBlockState(nextBlockPosition);
        BlockType nextTreePart = getTreePart(nextBlockState.getBlock());

        // if the block in the specified direction relative to the parent block is a log, then cut it down too!
        if (treePartShouldBreak(nextTreePart))
            fellingAlgorithm(world.getBlockState(nextBlockPosition), nextBlockPosition, world, thePlayer, nextTreePart, paths);
    }

    private static BlockPos travelToBlock(BlockPos startingBlock, EnumFacing[] pathToFollow) {
        // current block
        BlockPos endBlock = startingBlock;

        // follow the path to the block position
        for (EnumFacing step : pathToFollow) {
            endBlock = endBlock.offset(step);
        }

        return endBlock;
    }

    private static void breakBlock(IBlockState blockState, BlockPos blockPosition, World world, EntityPlayer thePlayer, BlockType treePart) {

        world.setBlockToAir(blockPosition); // delete the block
        if (!thePlayer.capabilities.isCreativeMode)
            blockState.getBlock().dropBlockAsItem(world, blockPosition, blockState, 0); // drop the block
    }

    private static boolean mainHandBreaksWhenDamaged(EntityPlayer thePlayer, BlockType treePart) {
        if (thePlayer.isCreative()) return false;
        if (!toolBreaksWhenDamaged((EntityPlayerMP) thePlayer, thePlayer.getHeldItemMainhand(), treePart)) return false;

        thePlayer.inventory.deleteStack(thePlayer.getHeldItemMainhand());
        return true;
    }

    private static boolean toolBreaksWhenDamaged(EntityPlayerMP thePlayer, ItemStack theTool, BlockType treePart) {
        return theTool.attemptDamageItem(treePart == BlockType.LEAF ? Configuration.serverSide.durabilityDamage * Configuration.serverSide.leafMultiplier / 100 : Configuration.serverSide.durabilityDamage, new Random(), thePlayer);
    }
}