package de.cottisystem.zauberstab.combination;

import de.cottisystem.zauberstab.model.Staff;
import de.cottisystem.zauberstab.model.Ability;
import de.cottisystem.zauberstab.model.Element;

import java.util.*;

/**
 * Kombinations-Engine für Fähigkeits-Berechnung.
 * Berechnet die resultierende Fähigkeit basierend auf Slot-Belegung und Tags.
 */
public class CombinationEngine {

    // Balance-Caps für PvE
    private static final int PVE_DAMAGE_CAP = 150; // +150% Basiswert
    private static final int PVE_RANGE_CAP = 15;   // max. 15 Blöcke
    private static final int PVE_AOE_CAP = 6;      // max. 6 Blöcke Radius
    private static final int PVE_DEBUFF_DURATION_CAP = 8; // max. 8 Sekunden
    private static final int PVE_HEAL_CAP = 20;    // max. 20% Max-HP

    // Balance-Caps für PvP
    private static final int PVP_DAMAGE_CAP = 80;  // +80% Basiswert
    private static final int PVP_RANGE_CAP = 10;   // max. 10 Blöcke
    private static final int PVP_AOE_CAP = 4;      // max. 4 Blöcke Radius
    private static final int PVP_DEBUFF_DURATION_CAP = 4; // max. 4 Sekunden
    private static final int PVP_HEAL_CAP = 10;    // max. 10% Max-HP

    /**
     * Berechnet die resultierende Fähigkeit aus der aktuellen Slot-Belegung.
     */
    public static CombinationResult calculateCombination(Staff mainStaff, Staff offhandStaff, boolean isPvP) {
        CombinationResult result = new CombinationResult();

        // Sammle alle Tags aus beiden Stäben
        Set<String> allTags = new HashSet<>();
        
        if (mainStaff != null) {
            for (Ability ability : mainStaff.getAbilities()) {
                allTags.addAll(ability.getTags());
            }
        }

        if (offhandStaff != null) {
            for (Ability ability : offhandStaff.getAbilities()) {
                allTags.addAll(ability.getTags());
            }
        }

        // Berechne Effekte basierend auf Tags
        result.setTags(allTags);
        result.setDamageBonus(calculateDamageBonus(allTags, mainStaff, isPvP));
        result.setRange(calculateRange(allTags, isPvP));
        result.setAoeRadius(calculateAoeRadius(allTags, isPvP));
        result.setDebuffDuration(calculateDebuffDuration(allTags, isPvP));
        result.setHealAmount(calculateHealAmount(allTags, isPvP));

        // Berechne Element-Bonus
        if (mainStaff != null) {
            result.setPrimaryElement(mainStaff.getElement());
            result.setElementBonus(calculateElementBonus(allTags, mainStaff.getElement()));
        }

        return result;
    }

    /**
     * Berechnet den Schaden-Bonus basierend auf Tags.
     */
    private static int calculateDamageBonus(Set<String> tags, Staff staff, boolean isPvP) {
        int bonus = 0;

        // Basis-Bonus für Damage-Tag
        if (tags.contains("damage")) {
            bonus += 20;
        }

        // Zusatz-Bonus für Element-Matching
        if (staff != null && tags.contains(staff.getElement().getId().toLowerCase())) {
            bonus += 15;
        }

        // Zusatz-Bonus für AoE
        if (tags.contains("aoe")) {
            bonus += 10;
        }

        // Anwende Cap
        int cap = isPvP ? PVP_DAMAGE_CAP : PVE_DAMAGE_CAP;
        return Math.min(bonus, cap);
    }

    /**
     * Berechnet die Reichweite basierend auf Tags.
     */
    private static int calculateRange(Set<String> tags, boolean isPvP) {
        int range = 5; // Standard-Reichweite

        if (tags.contains("range")) {
            range += 5;
        }

        if (tags.contains("lightning")) {
            range += 3;
        }

        int cap = isPvP ? PVP_RANGE_CAP : PVE_RANGE_CAP;
        return Math.min(range, cap);
    }

    /**
     * Berechnet den AoE-Radius basierend auf Tags.
     */
    private static int calculateAoeRadius(Set<String> tags, boolean isPvP) {
        if (!tags.contains("aoe")) {
            return 0;
        }

        int radius = 3; // Standard AoE

        if (tags.contains("earth")) {
            radius += 1;
        }

        if (tags.contains("lightning")) {
            radius += 1;
        }

        int cap = isPvP ? PVP_AOE_CAP : PVE_AOE_CAP;
        return Math.min(radius, cap);
    }

    /**
     * Berechnet die Debuff-Dauer basierend auf Tags.
     */
    private static int calculateDebuffDuration(Set<String> tags, boolean isPvP) {
        if (!tags.contains("slow") && !tags.contains("stun") && !tags.contains("burn")) {
            return 0;
        }

        int duration = 3; // Standard-Dauer in Sekunden

        if (tags.contains("ice")) {
            duration += 2;
        }

        if (tags.contains("fire")) {
            duration += 1;
        }

        int cap = isPvP ? PVP_DEBUFF_DURATION_CAP : PVE_DEBUFF_DURATION_CAP;
        return Math.min(duration, cap);
    }

    /**
     * Berechnet die Heilmenge basierend auf Tags.
     */
    private static int calculateHealAmount(Set<String> tags, boolean isPvP) {
        if (!tags.contains("heal")) {
            return 0;
        }

        int heal = 5; // Standard-Heilung in % Max-HP

        if (tags.contains("water")) {
            heal += 3;
        }

        if (tags.contains("earth")) {
            heal += 2;
        }

        int cap = isPvP ? PVP_HEAL_CAP : PVE_HEAL_CAP;
        return Math.min(heal, cap);
    }

    /**
     * Berechnet den Element-Bonus.
     */
    private static int calculateElementBonus(Set<String> tags, Element element) {
        String elementId = element.getId().toLowerCase();
        
        if (tags.contains(elementId)) {
            return 25; // 25% Element-Bonus
        }

        return 0;
    }

    /**
     * Gibt die Balance-Caps für PvE zurück.
     */
    public static Map<String, Integer> getPvECaps() {
        Map<String, Integer> caps = new HashMap<>();
        caps.put("damage", PVE_DAMAGE_CAP);
        caps.put("range", PVE_RANGE_CAP);
        caps.put("aoe", PVE_AOE_CAP);
        caps.put("debuff_duration", PVE_DEBUFF_DURATION_CAP);
        caps.put("heal", PVE_HEAL_CAP);
        return caps;
    }

    /**
     * Gibt die Balance-Caps für PvP zurück.
     */
    public static Map<String, Integer> getPvPCaps() {
        Map<String, Integer> caps = new HashMap<>();
        caps.put("damage", PVP_DAMAGE_CAP);
        caps.put("range", PVP_RANGE_CAP);
        caps.put("aoe", PVP_AOE_CAP);
        caps.put("debuff_duration", PVP_DEBUFF_DURATION_CAP);
        caps.put("heal", PVP_HEAL_CAP);
        return caps;
    }
}
