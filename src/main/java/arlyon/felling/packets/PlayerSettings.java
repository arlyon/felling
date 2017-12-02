package arlyon.felling.packets;

/**
 * A simple class that handles a player's settings on the server.
 */
public class PlayerSettings {
    public boolean disableWhenCrouched;
    public boolean disableWhenStanding;

    public PlayerSettings(boolean disableWhenCrouched, boolean disableWhenStanding) {
        this.disableWhenCrouched = disableWhenCrouched;
        this.disableWhenStanding = disableWhenStanding;
    }
}