package arlyon.felling;

import arlyon.felling.packets.PlayerSettings;
import net.minecraft.inventory.EntityEquipmentSlot;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String MOD_NAME = "Felling";
    public static final String MODID = "felling";
    public static final String VERSION = "1.3.0";
    public static final Enchantment felling = new Enchantment(net.minecraft.enchantment.Enchantment.Rarity.UNCOMMON, EntityEquipmentSlot.MAINHAND);

    public static final Map<Integer, PlayerSettings> playerSettings = new HashMap<>();
}