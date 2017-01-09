package arlyon.felling.enchantment;

import com.google.common.base.Predicate;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraftforge.common.util.EnumHelper;

public class EnchantmentFelling extends Enchantment
{

    private static Predicate<Item> axe_test = new Predicate<Item>() {
        @Override public boolean apply(Item item) {
            return item instanceof ItemAxe;
        }
    };

    private static EnumEnchantmentType AXE = EnumHelper.addEnchantmentType("AXE", axe_test);

    public EnchantmentFelling(Rarity rarityIn, EntityEquipmentSlot... slots)
    {
        super(rarityIn, AXE, slots);
        this.setName("felling");
        this.setRegistryName("felling");
    }

    /**
     * Returns the minimal value of enchantability needed on the enchantment level passed.
     */
    public int getMinEnchantability(int enchantmentLevel)
    {
        return 25;
    }

    /**
     * Returns the maximum value of enchantability needed on the enchantment level passed.
     */
    public int getMaxEnchantability(int enchantmentLevel)
    {
        return this.getMinEnchantability(enchantmentLevel) + 75;
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
    public int getMaxLevel()
    {
        return 1;
    }
}