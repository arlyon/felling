package arlyon.felling.events;

import arlyon.felling.Configuration;
import arlyon.felling.Constants;
import arlyon.felling.packets.MyMessage;
import arlyon.felling.packets.PacketHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class PlayerLoginEventHandler {
    @SubscribeEvent
    public void registerPlayerSettings(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        PacketHandler.INSTANCE.sendToServer(new MyMessage(Configuration.disableWhenCrouched, Configuration.disableWhenStanding));
    }
}
