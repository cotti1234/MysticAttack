package de.cottisystem.zauberstab.combination;

import de.cottisystem.zauberstab.model.Element;

import java.util.Set;

/**
 * Ergebnis einer Kombinations-Berechnung.
 * Speichert alle berechneten Werte für eine Fähigkeits-Kombination.
 */
public class CombinationResult {

    // Tags, die in dieser Kombination vorhanden sind
    private Set<String> tags;

    // Primäres Element
    private Element primaryElement;

    // Berechnete Effekte
    private int damageBonus;      // +X% Schaden
    private int range;             // Reichweite in Blöcken
    private int aoeRadius;         // AoE-Radius in Blöcken
    private int debuffDuration;    // Debuff-Dauer in Sekunden
    private int healAmount;        // Heilmenge in % Max-HP
    private int elementBonus;      // Element-Bonus in %

    /**
     * Standardkonstruktor für CombinationResult.
     */
    public CombinationResult() {
    }

    /**
     * Konstruktor mit Parametern.
     */
    public CombinationResult(Set<String> tags, Element primaryElement) {
        this.tags = tags;
        this.primaryElement = primaryElement;
    }

    /**
     * Gibt eine lesbare Beschreibung der Kombination zurück.
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();

        if (damageBonus > 0) {
            sb.append("Schaden: +").append(damageBonus).append("% | ");
        }

        if (range > 0) {
            sb.append("Reichweite: ").append(range).append(" Blöcke | ");
        }

        if (aoeRadius > 0) {
            sb.append("AoE: ").append(aoeRadius).append(" Blöcke | ");
        }

        if (debuffDuration > 0) {
            sb.append("Debuff: ").append(debuffDuration).append("s | ");
        }

        if (healAmount > 0) {
            sb.append("Heilung: ").append(healAmount).append("% | ");
        }

        if (elementBonus > 0) {
            sb.append("Element-Bonus: +").append(elementBonus).append("% | ");
        }

        // Entferne letztes " | "
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 3);
        }

        return sb.toString();
    }

    /**
     * Gibt die Anzahl der aktiven Effekte zurück.
     */
    public int getActiveEffectCount() {
        int count = 0;
        if (damageBonus > 0) count++;
        if (range > 0) count++;
        if (aoeRadius > 0) count++;
        if (debuffDuration > 0) count++;
        if (healAmount > 0) count++;
        if (elementBonus > 0) count++;
        return count;
    }

    /**
     * Prüft, ob diese Kombination einen bestimmten Effekt hat.
     */
    public boolean hasEffect(String effectType) {
        return switch (effectType.toLowerCase()) {
            case "damage" -> damageBonus > 0;
            case "range" -> range > 0;
            case "aoe" -> aoeRadius > 0;
            case "debuff" -> debuffDuration > 0;
            case "heal" -> healAmount > 0;
            case "element" -> elementBonus > 0;
            default -> false;
        };
    }

    // ===== GETTER UND SETTER FÜR ALLE FELDER =====
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Element getPrimaryElement() {
        return primaryElement;
    }

    public void setPrimaryElement(Element primaryElement) {
        this.primaryElement = primaryElement;
    }

    public int getDamageBonus() {
        return damageBonus;
    }

    public void setDamageBonus(int damageBonus) {
        this.damageBonus = Math.max(0, damageBonus);
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = Math.max(0, range);
    }

    public int getAoeRadius() {
        return aoeRadius;
    }

    public void setAoeRadius(int aoeRadius) {
        this.aoeRadius = Math.max(0, aoeRadius);
    }

    public int getDebuffDuration() {
        return debuffDuration;
    }

    public void setDebuffDuration(int debuffDuration) {
        this.debuffDuration = Math.max(0, debuffDuration);
    }

    public int getHealAmount() {
        return healAmount;
    }

    public void setHealAmount(int healAmount) {
        this.healAmount = Math.max(0, healAmount);
    }

    public int getElementBonus() {
        return elementBonus;
    }

    public void setElementBonus(int elementBonus) {
        this.elementBonus = Math.max(0, elementBonus);
    }
}
