package arlyon.felling.core.proxy;

import arlyon.felling.Enchantment;
import arlyon.felling.events.PlayerLoginEventHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Arrays;

/**
 * Handles things that should only happen on the client side.
 */
public class ProxyClient extends ProxyCommon {

    /**
     * Adds the enchantment type to the creative tabs client side,
     * and adds the client side event handler to send the config.
     * @param e The pre-initialization event.
     */
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        EnumEnchantmentType[] enchantmentTypes = CreativeTabs.TOOLS.getRelevantEnchantmentTypes();
        enchantmentTypes = Arrays.copyOf(CreativeTabs.TOOLS.getRelevantEnchantmentTypes(), enchantmentTypes.length+1);
        enchantmentTypes[enchantmentTypes.length-1] = Enchantment.AXE;
        CreativeTabs.TOOLS.setRelevantEnchantmentTypes(enchantmentTypes);

        MinecraftForge.EVENT_BUS.register(new PlayerLoginEventHandler());
    }

    @Override
    public void init(FMLInitializationEvent e) { super.init(e); }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

}
