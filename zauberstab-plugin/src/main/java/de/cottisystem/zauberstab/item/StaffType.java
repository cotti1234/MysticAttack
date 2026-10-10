package de.cottisystem.zauberstab.item;

import org.bukkit.Material;
import de.cottisystem.zauberstab.model.Element;
import de.cottisystem.zauberstab.model.StaffTier;

/**
 * Vordefinierte Stab-Typen mit Material und Element.
 */
public enum StaffType {
    BASIC("basic", "Basic-Stab", Material.STICK, Element.NEUTRAL, StaffTier.LOW, "Ein einfacher Stab"),
    WATER("water", "Wasser-Stab", Material.TRIDENT, Element.WATER, StaffTier.MEDIUM, "Ein Stab mit Wasser-Element"),
    OBSIDIAN("obsidian", "Obsidian-Stab", Material.DIAMOND_PICKAXE, Element.STONE, StaffTier.HIGH, "Ein mächtiger Obsidian-Stab");

    private final String id;
    private final String displayName;
    private final Material material;
    private final Element element;
    private final StaffTier tier;
    private final String description;

    StaffType(String id, String displayName, Material material, Element element, StaffTier tier, String description) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.element = element;
        this.tier = tier;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public Element getElement() {
        return element;
    }

    public StaffTier getTier() {
        return tier;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Gibt einen StaffType anhand der ID zurück.
     */
    public static StaffType fromId(String id) {
        for (StaffType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return BASIC;
    }
}
