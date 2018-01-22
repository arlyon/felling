package arlyon.felling;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

/**
 * The enchantment class for the Felling enchantment.
 */
public class FellingEnchantment extends net.minecraft.enchantment.Enchantment {

    /**
     * The AXE enchantment type.
     *
     * An axe enchantment type can be added to a tool
     * if the tool class "axe" exists on the tool.
     */
    public static final EnumEnchantmentType AXE = EnumHelper.addEnchantmentType("AXE", item ->
            item != null &&
            item.getToolClasses(new ItemStack(item)).stream().anyMatch(toolClass -> toolClass.equals("axe"))
    );

    /**
     * Sets name and registry name and assigns the proper predicate.
     *
     * @param rarityIn The rarity of the enchantment.
     * @param slots The slots in which the enchantment is valid.
     */
    FellingEnchantment(Rarity rarityIn, EntityEquipmentSlot... slots) {
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
     * @return Minimum value of enchantability for the given enchantment level.
     */
    public int getMinEnchantability(int enchantmentLevel) {
        return ((5 + (enchantmentLevel) * 10) * Configuration.serverSide.enchantmentRarity) / 100;
    }

    /**
     * Felling I - 25
     * Felling II - 35
     * Felling III - 45
     *
     * @param enchantmentLevel The level you want to get maximum enchantability weight for.
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