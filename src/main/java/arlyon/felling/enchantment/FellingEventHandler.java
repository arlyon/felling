package arlyon.felling.enchantment;

import arlyon.felling.Constants;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
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

public class FellingEventHandler {
    @SubscribeEvent
    public void fellTreeSubscriber(BlockEvent.BreakEvent event) {

        int damage = 0;

        EntityPlayer player = event.getPlayer();
        ItemStack item = player.getHeldItemMainhand();
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = event.getState();
        if (EnchantmentHelper.getEnchantmentLevel(Constants.felling, item)==1 && state.getBlock() == Blocks.LOG) {
            fellTree(state, pos, world, item, player);
        }
    }
    private void fellTree(IBlockState state, BlockPos pos, World worldIn, ItemStack item, EntityPlayer player) {
        state.getBlock().dropBlockAsItem(worldIn, pos, state, 0);
        worldIn.setBlockToAir(pos);
        if (item.attemptDamageItem(3, new Random())) {
            player.inventory.deleteStack(item);
            return;
        }

        BlockPos up_one = pos.offset(EnumFacing.UP);
        IBlockState up = worldIn.getBlockState(up_one);

        if (up.getBlock() == Blocks.LOG) {
            fellTree(up, up_one, worldIn, item, player);
        }

    }
}
