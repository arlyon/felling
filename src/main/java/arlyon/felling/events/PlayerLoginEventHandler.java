package arlyon.felling.events;

import arlyon.felling.Configuration;
import arlyon.felling.packets.FellingSettingsMessage;
import arlyon.felling.packets.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerLoginEventHandler {

    /**
     * Sends a packet to the server when the client connects with the client's felling settings.
     *
     * @param event The event that is called when an entity joins the world.
     */
    @SubscribeEvent
    public void registerPlayerSettings(EntityJoinWorldEvent event) {
        if (event.getEntity() == Minecraft.getMinecraft().player) {
            PacketHandler.INSTANCE.sendToServer(new FellingSettingsMessage(Configuration.clientSide.disableWhenCrouched, Configuration.clientSide.disableWhenStanding));
        }
    }
}
