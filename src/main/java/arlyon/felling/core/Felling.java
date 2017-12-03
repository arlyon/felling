package arlyon.felling.core;

import arlyon.felling.Constants;
import arlyon.felling.core.proxy.ProxyCommon;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

/**
 * The primary declaration file for the mod.
 */
@Mod(modid = Constants.MODID, name = Constants.MOD_NAME, version = Constants.VERSION, updateJSON="https://git.arlyon.co/minecraft/Felling/snippets/14/raw")
public class Felling {

    @SidedProxy(clientSide = "arlyon.felling.core.proxy.ProxyClient", serverSide = "arlyon.felling.core.proxy.ProxyServer")
    private static ProxyCommon proxy;

    public static Logger log;

    /**
     * Passes the pre-initialization event onwards to the proxy.
     * @param e The pre-initialization event.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
        log = e.getModLog();
    }

    /**
     * Passes the initialization event onwards to the proxy.
     * @param e The initialization event.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) { proxy.init(e); }

    /**
     * Passes the post-initialization event onwards to the proxy.
     * @param e The post-initialization event.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) { proxy.postInit(e); }

    /**
     * Sets up some event handlers.
     */
    @Mod.EventBusSubscriber(modid = Constants.MODID)
    public static class EventSubscriber {

        /**
         * Registers the felling enchantment when the Enchantment register event fires.
         * @param event The enchantment register event.
         */
        @SubscribeEvent
        public static void registerEnchantment(RegistryEvent.Register<net.minecraft.enchantment.Enchantment> event) {
            event.getRegistry().register(Constants.felling);
        }
    }
}
