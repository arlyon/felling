package arlyon.felling;

import arlyon.felling.proxy.ProxyCommon;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Constants.MODID, name = Constants.MOD_NAME, version = Constants.VERSION, updateJSON="https://git.arlyon.co/minecraft/Felling/snippets/14/raw")
public class Felling {

    @SidedProxy(clientSide = "arlyon.felling.proxy.ProxyClient", serverSide = "arlyon.felling.proxy.ProxyServer")
    private static ProxyCommon proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);

        MinecraftForge.EVENT_BUS.register(new EnchantmentEventHandler());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) { proxy.init(e); }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) { proxy.postInit(e); }

    @Mod.EventBusSubscriber(modid = Constants.MODID)
    public static class EnchantmentHandler {
        @SubscribeEvent
        public static void registerEnchantment(RegistryEvent.Register<net.minecraft.enchantment.Enchantment> event) {
            event.getRegistry().register(Constants.felling);
        }
    }
}
