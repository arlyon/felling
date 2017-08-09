package arlyon.felling.packets;

public class PlayerSettings {
    public boolean disableWhenCrouched;
    public boolean disableWhenStanding;

    public PlayerSettings(boolean disableWhenCrouched, boolean disableWhenStanding) {
        this.disableWhenCrouched = disableWhenCrouched;
        this.disableWhenStanding = disableWhenStanding;
    }
}