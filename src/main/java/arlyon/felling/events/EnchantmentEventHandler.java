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

import java.util.Arrays;
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

    private static int logID = OreDictionary.getOreID("logWood");
    private static int leafID = OreDictionary.getOreID("treeLeaves");

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
                currentEvent.getState(),
                currentEvent.getPos(),
                currentEvent.getWorld(),
                currentEvent.getPlayer(),
                getTreePart(currentEvent.getState().getBlock()),
                getPaths(currentEvent));
    }

    /**
     * Gets the block type of the block.
     *
     * @param block The block to test.
     * @return The block type.
     */
    private static BlockType getTreePart(Block block) {
        return getTreePart(new ItemStack(block, 1));
    }

    /**
     * Gets the block type of the item stack.
     *
     * @param stack the stack to test.
     * @return The block type.
     */
    private static BlockType getTreePart(ItemStack stack) {
        if (stack.isEmpty()) return null;

        int[] blockIDs = OreDictionary.getOreIDs(stack);

        return Arrays.stream(blockIDs)
                .filter(id -> id == logID || id == leafID)
                .mapToObj(id -> id == logID ? BlockType.LOG : BlockType.LEAF)
                .findFirst()
                .orElse(null);
    }

    /**
     * Registers blocks that don't follow conventions on a case-by-base basis.
     *
     * @param stack The item stack to register.
     */
    private static void registerIncompatibleBlocks(ItemStack stack) {
        if (stack.getUnlocalizedName().matches("^ic2.rubber_wood$")) {
            OreDictionary.registerOre("logWood", stack);
        }
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
        PlayerSettings playerSettings = getOrCreatePlayerSettings(event.getPlayer());
        return event.getPlayer().isSneaking() ? !playerSettings.disableWhenCrouched : !playerSettings.disableWhenStanding;
    }

    /**
     * Given a player, gets or creates a player settings profile for a user.
     *
     * @param thePlayer The player to check.
     * @return The given player's settings.
     * TODO maybe move this somewhere else.
     */
    private PlayerSettings getOrCreatePlayerSettings(EntityPlayer thePlayer) {
        PlayerSettings playerSettings = Constants.playerSettings.get(thePlayer.getGameProfile().hashCode());

        if (playerSettings == null) {
            playerSettings = new PlayerSettings(true, true);
            thePlayer.sendMessage(new TextComponentString("Your Felling settings aren't synced with the server. Please update the settings in the mod config to resend them."));
        }

        return playerSettings;
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
     * A simple check to see if the player that caused the break event has the enchantment in their mainhand.
     *
     * @param event The block break event.
     * @return Whether the player's main hand is enchanted.
     */
    private boolean mainHandHasEnchantment(BlockEvent.BreakEvent event) {
        return EnchantmentHelper.getEnchantmentLevel(Constants.felling, event.getPlayer().getHeldItemMainhand()) != 0;
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
     * Gets the list of valid felling paths given an event.
     *
     * @param event The break event.
     * @return The list of paths matching the felling enchantment version.
     */
    private static EnumFacing[][] getPaths(BlockEvent.BreakEvent event) {
        return fellingPaths[EnchantmentHelper.getEnchantmentLevel(Constants.felling, event.getPlayer().getHeldItemMainhand()) - 1];
    }

    /**
     * Breaks the block at a given position and then for
     * each path continues felling on that block as well.
     *
     * @param blockState    The state of the block.
     * @param blockPosition The position of the block.
     * @param world         The world.
     * @param thePlayer     The player.
     * @param treePart      The tree part.
     * @param paths         The paths to fell.
     */
    private static void fellingAlgorithm(IBlockState blockState, BlockPos blockPosition, World world, EntityPlayer thePlayer, BlockType treePart, EnumFacing[][] paths) {
        // try to break the block and if it fails then return

        breakBlock(blockState, blockPosition, world, thePlayer);
        if (mainHandBreaksWhenDamaged(thePlayer, treePart)) return;

        // for each path passed in, travel to the block and test it
        for (EnumFacing[] path : paths) {
            continueFelling(blockPosition, world, thePlayer, paths, path);
        }
    }

    /**
     * Given a path, follows it and calls felling algorithm again.
     *
     * @param currentBlockPosition The current position.
     * @param world                The world.
     * @param thePlayer            The player.
     * @param paths                The list of paths.
     * @param pathToFollow         The path to follow to get to the next block.
     */
    private static void continueFelling(BlockPos currentBlockPosition, World world, EntityPlayer thePlayer, EnumFacing[][] paths, EnumFacing[] pathToFollow) {

        BlockPos nextBlockPosition = travelToBlock(currentBlockPosition, pathToFollow);
        IBlockState nextBlockState = world.getBlockState(nextBlockPosition);
        BlockType nextTreePart = getTreePart(nextBlockState.getBlock());

        if (treePartShouldBreak(nextTreePart))
            fellingAlgorithm(world.getBlockState(nextBlockPosition), nextBlockPosition, world, thePlayer, nextTreePart, paths);
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
     * Given a position, sets the block to air and drops the item.
     *
     * @param blockState    The state.
     * @param blockPosition The position.
     * @param world         The world.
     * @param thePlayer     The player.
     */
    private static void breakBlock(IBlockState blockState, BlockPos blockPosition, World world, EntityPlayer thePlayer) {

        world.setBlockToAir(blockPosition); // delete the block
        if (!thePlayer.capabilities.isCreativeMode)
            blockState.getBlock().dropBlockAsItem(world, blockPosition, blockState, 0); // drop the block
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
}