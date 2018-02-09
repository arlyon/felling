package arlyon.felling.proxy;

import arlyon.felling.FellingEnchantment;
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
     *
     * @param event The pre-initialization event.
     */
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        addEnchantToCreativeTab();
        MinecraftForge.EVENT_BUS.register(new PlayerLoginEventHandler());
    }

    /**
     * Registers the enchantment in the creative tab.
     */
    private void addEnchantToCreativeTab() {
        EnumEnchantmentType[] enchantmentTypes = CreativeTabs.TOOLS.getRelevantEnchantmentTypes();
        enchantmentTypes = Arrays.copyOf(enchantmentTypes, enchantmentTypes.length + 1);
        enchantmentTypes[enchantmentTypes.length - 1] = FellingEnchantment.AXE;
        CreativeTabs.TOOLS.setRelevantEnchantmentTypes(enchantmentTypes);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }
}
