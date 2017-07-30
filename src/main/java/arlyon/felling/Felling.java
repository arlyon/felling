package arlyon.felling;

import arlyon.felling.proxy.ProxyCommon;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Constants.MODID, name = Constants.MOD_NAME, version = Constants.VERSION, updateJSON="https://git.arlyon.co/minecraft/Felling/snippets/14/raw")
public class Felling {

    @SidedProxy(clientSide = "arlyon.felling.proxy.ProxyClient", serverSide = "arlyon.felling.proxy.ProxyServer")
    public static ProxyCommon proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) { proxy.preInit(e); }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) { proxy.init(e); }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) { proxy.postInit(e); }

}
