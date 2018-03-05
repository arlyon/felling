package arlyon.felling;

import arlyon.felling.network.PlayerSettings;
import arlyon.felling.proxy.ProxyCommon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * The primary declaration file for the mod.
 */
@Mod(
        modid = Felling.MOD_ID,
        name = Felling.MOD_NAME,
        version = Felling.VERSION,
        updateJSON = Felling.UPDATE_JSON,
        acceptedMinecraftVersions = Felling.MINECRAFT_VERSIONS
)
public class Felling {

    public static final FellingEnchantment felling = new FellingEnchantment(net.minecraft.enchantment.Enchantment.Rarity.UNCOMMON, EntityEquipmentSlot.MAINHAND);
    public static final Map<Integer, PlayerSettings> playerSettings = new HashMap<>();
    static final String MOD_NAME = "Felling";
    static final String MOD_ID = "felling";
    static final String VERSION = "1.3.3";
    static final String UPDATE_JSON = "https://raw.githubusercontent.com/arlyon/felling/1.12.x/update.json";
    static final String MINECRAFT_VERSIONS = "[1.12.0, 1.12.2]"; // starting with 1.12, up to 1.12.2
    public static Logger log;

    @SidedProxy(clientSide = "arlyon.felling.proxy.ProxyClient", serverSide = "arlyon.felling.proxy.ProxyServer")
    private static ProxyCommon proxy;

    /**
     * Passes the pre-initialization event onwards to the proxy.
     *
     * @param e The pre-initialization event.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
        log = e.getModLog();
    }

    /**
     * Passes the initialization event onwards to the proxy.
     *
     * @param e The initialization event.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    /**
     * Passes the post-initialization event onwards to the proxy.
     *
     * @param e The post-initialization event.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }

    /**
     * Sets up some event handlers.
     */
    @Mod.EventBusSubscriber
    public static class EventSubscriber {

        /**
         * Registers the felling enchantment when the FellingEnchantment register event fires.
         *
         * @param event The enchantment register event.
         */
        @SubscribeEvent
        public static void registerEnchantment(RegistryEvent.Register<net.minecraft.enchantment.Enchantment> event) {
            event.getRegistry().register(felling);
        }
    }

    /**
     * Given a player, gets or creates a player settings profile for a user.
     *
     * @param thePlayer The player to check.
     * @return The given player's settings.
     */
    public static PlayerSettings getOrCreatePlayerSettings(EntityPlayer thePlayer) {
        PlayerSettings playerSettings = Felling.playerSettings.get(thePlayer.getGameProfile().hashCode());

        if (playerSettings == null) {
            playerSettings = new PlayerSettings(true, true);
            thePlayer.sendMessage(new TextComponentString("Your Felling settings aren't synced with the server. Please update the settings in the mod config to resend them."));
        }

        return playerSettings;
    }
}
