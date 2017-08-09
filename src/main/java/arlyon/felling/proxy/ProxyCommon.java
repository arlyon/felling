package arlyon.felling.proxy;

import arlyon.felling.packets.PacketHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ProxyCommon {

    public void preInit(FMLPreInitializationEvent e) {

        PacketHandler.registerMessages("felling");

    }

    public void init(FMLInitializationEvent e) { }

    public void postInit(FMLPostInitializationEvent e) { }
}
