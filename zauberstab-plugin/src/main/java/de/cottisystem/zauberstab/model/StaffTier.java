package de.cottisystem.zauberstab.model;


/**
 * Stab-Tier bestimmt Slot-Anzahl und Qualität.
 */
public enum StaffTier {
    LOW("Niedrig", 2),
    MEDIUM("Mittel", 3),
    HIGH("Hoch", 5);

    private final String displayName;
    private final int slotCount;

    StaffTier(String displayName, int slotCount) {
        this.displayName = displayName;
        this.slotCount = slotCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public String toString() {
        return displayName + " (" + slotCount + " Slots)";
    }
}
