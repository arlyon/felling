package arlyon.felling.proxy;

import arlyon.felling.enchantment.EnchantmentFelling;
import arlyon.felling.enchantment.FellingEventHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon {

    public void preInit(FMLPreInitializationEvent e) { }

    public void init(FMLInitializationEvent e) {
        GameRegistry.register(new EnchantmentFelling(Enchantment.Rarity.COMMON, EntityEquipmentSlot.MAINHAND));
    }

    public void postInit(FMLPostInitializationEvent e) {
        // EVENT HANDLER
        FellingEventHandler fellevent = new FellingEventHandler();
        MinecraftForge.EVENT_BUS.register(fellevent);
    }

}
