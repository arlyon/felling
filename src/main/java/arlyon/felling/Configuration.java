package arlyon.felling;

import arlyon.felling.network.FellingSettingsMessage;
import arlyon.felling.network.PacketHandler;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Controls the configurable options in the mod config menu.
 */
@Config(modid = Felling.MOD_ID)
public class Configuration {

    @Config.Name("Server-side Settings")
    @Config.Comment("These settings only affect you if you are hosting the game.")
    @Config.LangKey("felling.config.server")
    public static ServerSide serverSide = new ServerSide();

    @Config.Name("Client-side Settings")
    @Config.Comment("These settings are personal to you and apply to all games.")
    @Config.LangKey("felling.config.client")
    public static ClientSide clientSide = new ClientSide();

    /**
     * The server side settings.
     */
    public static class ServerSide {

        @Config.Name("Include Leaves")
        @Config.Comment("Determines whether or not leaves should be included when the enchantment cuts down a tree.")
        @Config.LangKey("felling.config.server.cutLeaves")
        public boolean cutLeaves = false;

        @Config.Name("Felling Durability Cost")
        @Config.Comment("Controls how much damage is done to the axe per log when the enchantment topples a tree.")
        @Config.LangKey("felling.config.server.durabilityDamage")
        @Config.RangeInt(min=0, max=5)
        public int durabilityDamage = 2;

        @Config.Name("Leaf Cost Multiplier")
        @Config.Comment("Controls the amount of damage that cutting leaves causes to your axe in relation to the value set for logs. The max (200%) is double durability damage compared to normal logs, with 0% being no damage.")
        @Config.LangKey("felling.config.server.leafMultiplier")
        @Config.RangeInt(min=0, max=200)
        public int leafMultiplier = 100;

        @Config.Name("Rarity (%)")
        @Config.Comment("Controls how rare the enchantment is (with 100% being as the mod was intended). It is recommended to keep it between 80% and 120%, and more statistics can be found on the wiki.")
        @Config.LangKey("felling.config.server.enchantmentRarity")
        @Config.RangeInt(min=0, max=200)
        public int enchantmentRarity = 100;

        @Config.Name("Maximum Blocks To Break")
        @Config.Comment("Puts a limit on the number of blocks to break. 0 for no limit.")
        @Config.LangKey("felling.config.server.maxBlocks")
        @Config.RangeInt(min=0)
        public int maxBlocks = 0;

    }

    /**
     * The client side settings.
     */
    public static class ClientSide {

        @Config.Name("Disable When Crouching")
        @Config.Comment("When true, the enchantment won't take effect when crouched.")
        @Config.LangKey("felling.config.client.crouched")
        public boolean disableWhenCrouched = true;

        @Config.Name("Disable When Standing")
        @Config.Comment("When true, the enchantment won't take effect when stood up.")
        @Config.LangKey("felling.config.client.standing")
        public boolean disableWhenStanding = false;

    }

    /**
     * Sets up some event handlers.
     */
    @Mod.EventBusSubscriber(modid = Felling.MOD_ID)
    private static class EventHandler {

        /**
         * Saves the config locally and also sends critical values to the server when the config changes.
         * @param event The config changed event.
         */
        @SubscribeEvent
        public static void onConfigChanged(OnConfigChangedEvent event) {
            if (event.getModID().equals(Felling.MOD_ID)) {
                ConfigManager.sync(Felling.MOD_ID, Config.Type.INSTANCE);

                PacketHandler.INSTANCE.sendToServer(
                        new FellingSettingsMessage(
                                clientSide.disableWhenCrouched,
                                clientSide.disableWhenStanding
                        )
                );
            }
        }
    }
}
