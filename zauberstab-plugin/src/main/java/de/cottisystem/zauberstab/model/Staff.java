package de.cottisystem.zauberstab.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Stab (Staff) - das Hauptwerkzeug des Spielers.
 * Hat einen Typ, Element, Tier und Slots für Fähigkeiten.
 */
public class Staff {

    private String id;
    private String displayName;
    private String description;
    
    // Element des Stabes (bestimmt Bonusse)
    private Element element;
    
    // Tier bestimmt Slot-Anzahl
    private StaffTier tier;
    
    // Slots mit Fähigkeiten
    private List<Ability> abilities = new ArrayList<>();
    
    // Durability (0-100)
    private int durability;
    
    // Maximale Durability
    private int maxDurability;
    
    // Spezielle Effekte (z.B. Blindheit beim Obsidian-Stab)
    private List<String> specialEffects = new ArrayList<>();

    /**
     * Standardkonstruktor für Staff.
     */
    public Staff() {
    }

    /**
     * Konstruktor mit allen Parametern.
     */
    public Staff(String id, String displayName, String description, Element element, StaffTier tier, int maxDurability) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.element = element;
        this.tier = tier;
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
    }

    // Getter und Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public StaffTier getTier() {
        return tier;
    }

    public void setTier(StaffTier tier) {
        this.tier = tier;
    }

    public List<Ability> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public void setMaxDurability(int maxDurability) {
        this.maxDurability = maxDurability;
    }

    public List<String> getSpecialEffects() {
        return specialEffects;
    }

    public void setSpecialEffects(List<String> specialEffects) {
        this.specialEffects = specialEffects;
    }

    /**
     * Gibt die Anzahl der verfügbaren Slots zurück.
     */
    public int getSlotCount() {
        return tier.getSlotCount();
    }

    /**
     * Gibt die Anzahl der belegten Slots zurück.
     */
    public int getUsedSlots() {
        return abilities.size();
    }

    /**
     * Prüft, ob noch Slots verfügbar sind.
     */
    public boolean hasAvailableSlots() {
        return getUsedSlots() < getSlotCount();
    }

    /**
     * Fügt eine Fähigkeit hinzu (falls Platz vorhanden).
     */
    public boolean addAbility(Ability ability) {
        if (hasAvailableSlots()) {
            abilities.add(ability);
            return true;
        }
        return false;
    }

    /**
     * Entfernt eine Fähigkeit.
     */
    public boolean removeAbility(Ability ability) {
        return abilities.remove(ability);
    }

    /**
     * Gibt die Fähigkeit an einem bestimmten Slot zurück.
     */
    public Ability getAbilityAtSlot(int slot) {
        if (slot >= 0 && slot < abilities.size()) {
            return abilities.get(slot);
        }
        return null;
    }

    /**
     * Reduziert die Durability.
     */
    public void reduceDurability(int amount) {
        durability = Math.max(0, durability - amount);
    }

    /**
     * Repariert den Stab.
     */
    public void repair(int amount) {
        durability = Math.min(maxDurability, durability + amount);
    }

    /**
     * Prüft, ob der Stab beschädigt ist.
     */
    public boolean isDamaged() {
        return durability < maxDurability;
    }

    @Override
    public String toString() {
        return displayName + " (" + element.getDisplayName() + ", " + tier.getDisplayName() + ")";
    }
}
