package arlyon.felling;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Alexander Lyon on 30.07.2017.
 */

@Config(modid = Constants.MODID)
public class Configuration {

    @Config.Name("Include Leaves")
    @Config.Comment("Determines whether or not leaves should be included when the enchantment cuts down a tree.")
    public static boolean cutLeaves = false;

    @Config.Name("Disable When Crouching")
    @Config.Comment("When true, the enchantment won't take effect when crouched.")
    public static boolean disableWhenCrouched = true;

    @Config.Name("Disable When Standing")
    @Config.Comment("When true, the enchantment won't take effect when stood up.")
    public static boolean disableWhenStanding = false;

    @Config.Name("Felling Durability Cost")
    @Config.Comment("Controls how much damage is done to the axe per log when the enchantment topples a tree.")
    @Config.RangeInt(min=0, max=5)
    public static int durabilityDamage = 2;

    @Config.Name("Leaf Cost Multiplier")
    @Config.Comment("Controls the amount of damage that cutting leaves causes to your axe in relation to the value set for logs. The max (200%) is double durability damage compared to normal logs, with 0% being no damage.")
    @Config.RangeInt(min=0, max=200)
    public static int leafMultiplier = 100;

    @Mod.EventBusSubscriber
    private static class EventHandler {

        /**
         * Inject the new values and save to the config file when the config has been changed from the GUI.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Constants.MODID)) {
                ConfigManager.sync(Constants.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
