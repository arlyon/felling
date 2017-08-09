package arlyon.felling;

import arlyon.felling.packets.MyMessage;
import arlyon.felling.packets.PacketHandler;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Alexander Lyon on 30.07.2017.
 *
 * Controls the configurable options in the mod config menu.
 */

@Config(modid = Constants.MODID)
public class Configuration {

    @Config.Name("Server-side Settings")
    @Config.Comment("These settings only affect you if you are hosting the game.")
    public static ServerSide serverSide = new ServerSide();

    @Config.Name("Client-side Settings")
    @Config.Comment("These settings are personal to you and apply to all games.")
    public static ClientSide clientSide = new ClientSide();

    public static class ServerSide {

        @Config.Name("Include Leaves")
        @Config.Comment("Determines whether or not leaves should be included when the enchantment cuts down a tree. Convenient however can cause Stack Overflow errors in jungles.")
        public boolean cutLeaves = false;

        @Config.Name("Felling Durability Cost")
        @Config.Comment("Controls how much damage is done to the axe per log when the enchantment topples a tree.")
        @Config.RangeInt(min=0, max=5)
        public int durabilityDamage = 2;

        @Config.Name("Leaf Cost Multiplier")
        @Config.Comment("Controls the amount of damage that cutting leaves causes to your axe in relation to the value set for logs. The max (200%) is double durability damage compared to normal logs, with 0% being no damage.")
        @Config.RangeInt(min=0, max=200)
        public int leafMultiplier = 100;

        @Config.Name("Rarity (%)")
        @Config.Comment("Controls how rare the enchantment is (with 100% being as the mod was intended). It is recommended to keep it between 80% and 120%, and more statistics can be found on the wiki.")
        @Config.RangeInt(min=0, max=200)
        public int enchantmentRarity = 100;

    }

    public static class ClientSide {

        @Config.Name("Disable When Crouching")
        @Config.Comment("When true, the enchantment won't take effect when crouched.")
        public boolean disableWhenCrouched = true;

        @Config.Name("Disable When Standing")
        @Config.Comment("When true, the enchantment won't take effect when stood up.")
        public boolean disableWhenStanding = false;

    }

    @Mod.EventBusSubscriber
    private static class EventHandler {

        @SubscribeEvent
        public static void saveConfigOnChange(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Constants.MODID)) {
                ConfigManager.sync(Constants.MODID, Config.Type.INSTANCE);

                PacketHandler.INSTANCE.sendToServer(
                        new MyMessage(
                                clientSide.disableWhenCrouched,
                                clientSide.disableWhenStanding));
            }
        }
    }
}
