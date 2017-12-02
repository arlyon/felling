package arlyon.felling.events;

import arlyon.felling.Configuration;
import arlyon.felling.packets.FellingSettingsMessage;
import arlyon.felling.packets.PacketHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class PlayerLoginEventHandler {

    /**
     * Sends a packet to the server when the client connects with the client's felling settings.
     *
     * @param event The event that is called when the client connects.
     */
    @SubscribeEvent
    public void registerPlayerSettings(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        PacketHandler.INSTANCE.sendToServer(new FellingSettingsMessage(Configuration.clientSide.disableWhenCrouched, Configuration.clientSide.disableWhenStanding));
    }
}
