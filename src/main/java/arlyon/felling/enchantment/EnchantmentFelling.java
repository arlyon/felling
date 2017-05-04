package arlyon.felling.enchantment;

import com.google.common.base.Predicate;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraftforge.common.util.EnumHelper;

public class EnchantmentFelling extends Enchantment {

    private static Predicate<Item> isAxe = new Predicate<Item>() {
        @Override
        public boolean apply(Item item) {
            return item instanceof ItemAxe;
        }
    };

    private static EnumEnchantmentType AXE = EnumHelper.addEnchantmentType("AXE", isAxe);

    public EnchantmentFelling(Rarity rarityIn, EntityEquipmentSlot... slots) {
        super(rarityIn, AXE, slots);
        setName("felling");
        setRegistryName("felling");
    }

    /**
     * Returns the minimal value of enchantability needed on the enchantment level passed.
     *
     * Felling I - 20
     * Felling II - 35
     */
    public int getMinEnchantability(int enchantmentLevel) { return 5 + (enchantmentLevel) * 15; }

    /**
     * Returns the maximum value of enchantability needed on the enchantment level passed.
     */
    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 50;
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
    public int getMaxLevel() {
        return 2;
    }
}