package arlyon.felling.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Used to create a channel to send messages from the client to the server.
 */
public class PacketHandler {

    public static SimpleNetworkWrapper INSTANCE = null;
    private static int packetId = 0;

    public PacketHandler() {
    }

    /**
     * Called to set up a client->server channel on the packet handler.
     *
     * @param channelName The name of the channel.
     */
    public static void registerMessages(String channelName) {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
        registerMessages();
    }

    /**
     * Registers the messages which are to be sent from the client to the server.
     */
    private static void registerMessages() {
        INSTANCE.registerMessage(
                FellingSettingsMessage.Handler.class,
                FellingSettingsMessage.class,
                nextID(),
                Side.SERVER
        );
    }

    /**
     * Gets the next packet id.
     *
     * @return The next packet id.
     */
    private static int nextID() {
        return packetId++;
    }
}
