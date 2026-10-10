package de.cottisystem.zauberstab.crafting;

/**
 * Repräsentiert eine Fähigkeits-Schmiede (Forge Machine).
 * Verwaltet Upgrade-Versuche und Erfolgsquoten.
 */
public class ForgeMachine {

    // Erfolgsquote (90% = 0.9)
    private static final double SUCCESS_RATE = 0.9;
    
    // Durability-Schaden bei Fehlschlag
    private static final int FAILURE_DURABILITY_LOSS = 10;
    
    // Reparatur-Kosten (% der ursprünglichen Upgrade-Kosten)
    private static final double REPAIR_COST_MULTIPLIER = 0.5;

    /**
     * Führt einen Upgrade-Versuch durch.
     * Gibt true zurück, wenn erfolgreich.
     */
    public static boolean attemptUpgrade() {
        return Math.random() < SUCCESS_RATE;
    }

    /**
     * Gibt die Erfolgsquote als Prozentsatz zurück.
     */
    public static int getSuccessRatePercent() {
        return (int) (SUCCESS_RATE * 100);
    }

    /**
     * Gibt den Durability-Schaden bei Fehlschlag zurück.
     */
    public static int getFailureDurabilityLoss() {
        return FAILURE_DURABILITY_LOSS;
    }

    /**
     * Berechnet die Reparatur-Kosten basierend auf den Upgrade-Kosten.
     */
    public static int calculateRepairCost(int upgradeCost) {
        return (int) (upgradeCost * REPAIR_COST_MULTIPLIER);
    }

    /**
     * Gibt die Reparatur-Kosten-Multiplikator zurück.
     */
    public static double getRepairCostMultiplier() {
        return REPAIR_COST_MULTIPLIER;
    }
}
