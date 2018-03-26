package arlyon.felling.events;

import arlyon.felling.Felling;
import arlyon.felling.FellingAlgorithm;
import arlyon.felling.network.PlayerSettings;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Intercepts the block break event and calls the felling algorithm.
 */
public class FellingEventHandler {

    /**
     * Listens to block break events and checks to see if
     * we need to start the felling algorithm and runs it.
     *
     * @param event The break event.
     */
    @SubscribeEvent
    public void fellingBlockBreakSubscriber(BlockEvent.BreakEvent event) {
        if (shouldStartFelling(event)) FellingAlgorithm.fellingAlgorithm(
                event.getPos(),
                event.getWorld(),
                event.getPlayer()
        );
    }

    /**
     * Makes some checks to see if it is a valid felling event.
     *
     * @param event The break event.
     * @return Whether the felling should run.
     */
    private boolean shouldStartFelling(BlockEvent.BreakEvent event) {
        return (configAllowsBreak(event) &&
                eventIsServerSide(event) &&
                toolHasFelling(event));
    }

    /**
     * Checks the configs to see if the block should break by checking
     * if the player is standing or crouching and testing if the config
     * has the given state enabled or disabled.
     *
     * @param event The block break event.
     * @return Whether the config allows the break.
     */
    private boolean configAllowsBreak(BlockEvent.BreakEvent event) {
        PlayerSettings playerSettings = Felling.getOrCreatePlayerSettings(event.getPlayer());
        return event.getPlayer().isSneaking() ? !playerSettings.disableWhenCrouched : !playerSettings.disableWhenStanding;
    }

    /**
     * Checks if the event is server-side (mainly for readability).
     *
     * @param event The break event.
     * @return Whether the event is being called on the server side.
     */
    private boolean eventIsServerSide(BlockEvent.BreakEvent event) {
        return !event.getWorld().isRemote; // remote compared to the server
    }

    /**
     * A simple check to see if the player that caused the break event has the enchantment in their main hand.
     *
     * @param event The block break event.
     * @return Whether the player's main hand is enchanted.
     */
    private boolean toolHasFelling(BlockEvent.BreakEvent event) {
        return EnchantmentHelper.getEnchantmentLevel(Felling.felling, event.getPlayer().getHeldItemMainhand()) != 0;
    }
}