package com.dnocturne.basalt.component.effect;

import com.dnocturne.basalt.component.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Modifies player attributes (speed, damage, health, etc.).
 * Applied when component is applied, removed when it ends.
 *
 * @param <I> The instance type containing state data
 */
public class AttributeModifierComponent<I> implements Component<Player, I> {

    private final String id;
    private final Attribute attribute;
    private final double amount;
    private final AttributeModifier.Operation operation;
    private final NamespacedKey modifierKey;

    /**
     * Create an attribute modifier component.
     *
     * @param id          Component ID
     * @param attribute   The attribute to modify
     * @param amount      The modification amount
     * @param operation   The operation type (ADD_NUMBER, ADD_SCALAR, MULTIPLY_SCALAR_1)
     * @param modifierKey Unique key for the modifier (used for removal)
     */
    public AttributeModifierComponent(@NotNull String id, @NotNull Attribute attribute, double amount,
                                       @NotNull AttributeModifier.Operation operation, @NotNull NamespacedKey modifierKey) {
        this.id = id;
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
        this.modifierKey = modifierKey;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public void onApply(@NotNull Player player, @NotNull I instance) {
        AttributeInstance attrInstance = player.getAttribute(attribute);
        if (attrInstance == null) {
            return;
        }

        // Remove existing modifier if present
        attrInstance.removeModifier(modifierKey);

        // Add the new modifier
        AttributeModifier modifier = new AttributeModifier(modifierKey, amount, operation);
        attrInstance.addModifier(modifier);
    }

    @Override
    public void onRemove(@NotNull Player player, @NotNull I instance) {
        AttributeInstance attrInstance = player.getAttribute(attribute);
        if (attrInstance == null) {
            return;
        }

        attrInstance.removeModifier(modifierKey);
    }

    /**
     * Get the attribute being modified.
     */
    public @NotNull Attribute getAttribute() {
        return attribute;
    }

    /**
     * Get the modification amount.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Get the operation type.
     */
    public @NotNull AttributeModifier.Operation getOperation() {
        return operation;
    }

    /**
     * Get the modifier key.
     */
    public @NotNull NamespacedKey getModifierKey() {
        return modifierKey;
    }
}
