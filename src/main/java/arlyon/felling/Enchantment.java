package arlyon.felling;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

import java.util.Arrays;

/**
 * The enchantment class for the Felling enchantment.
 */
public class Enchantment extends net.minecraft.enchantment.Enchantment {

    /**
     * Axe enchantment type.
     */
    public static EnumEnchantmentType AXE = EnumHelper.addEnchantmentType("AXE", item -> {
        assert item != null;
        return item.getToolClasses(new ItemStack(item)).stream().anyMatch(toolClass -> toolClass.equals("axe"));
    });

    /**
     * Sets name and registry name and assigns the proper predicate.
     *
     * @param rarityIn the rarity of the enchantment
     * @param slots the slots in which the enchantment is valid
     */
    Enchantment(Rarity rarityIn, EntityEquipmentSlot... slots) {
        super(rarityIn, AXE, slots);
        setName("felling");
        setRegistryName("felling");
    }

    /**
     * Felling I - 15
     * Felling II - 25
     * Felling III - 35
     *
     * @param enchantmentLevel The level you want to get minimum enchantability weight for.
     * @return Minimus value of enchantability for the given enchantment level.
     */
    public int getMinEnchantability(int enchantmentLevel) { return ((5 + (enchantmentLevel) * 10) * Configuration.serverSide.enchantmentRarity) / 100; }

    /**
     * Felling I - 25
     * Felling II - 35
     * Felling III - 45
     *
     * @param enchantmentLevel The level you want to get maximun enchantability weight for.
     * @return Maximum value of enchantability for the given enchantment level.
     */
    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 10;
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
    public int getMaxLevel() {
        return 3;
    }
}