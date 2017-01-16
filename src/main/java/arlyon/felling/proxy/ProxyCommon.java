package arlyon.felling.proxy;

import arlyon.felling.Constants;
import arlyon.felling.enchantment.FellingEventHandler;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon {

    public void preInit(FMLPreInitializationEvent e) { }

    public void init(FMLInitializationEvent e) {
        // Enchantment
        GameRegistry.register(Constants.felling);
        // EVENT HANDLER
        FellingEventHandler fellevent = new FellingEventHandler();
        MinecraftForge.EVENT_BUS.register(fellevent);
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

}
