package arlyon.felling;

import arlyon.felling.enchantment.EnchantmentFelling;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EntityEquipmentSlot;

public class Constants {
    public static final String MOD_NAME = "Felling";
    public static final String MODID = "felling";
    public static final String VERSION = "1.1";
    public static final EnchantmentFelling felling = new EnchantmentFelling(Enchantment.Rarity.UNCOMMON, EntityEquipmentSlot.MAINHAND);
}