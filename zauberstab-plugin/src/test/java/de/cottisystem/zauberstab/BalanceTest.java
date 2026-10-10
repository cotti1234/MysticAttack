package de.cottisystem.zauberstab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.cottisystem.zauberstab.balance.BalanceConfig;
import de.cottisystem.zauberstab.balance.BalanceManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test-Suite für das Balancing-System.
 */
public class BalanceTest {

    private BalanceConfig balanceConfig;

    @BeforeEach
    public void setUp() {
        balanceConfig = new BalanceConfig();
    }

    @Test
    public void testWaterBlastDamage() {
        int damage1 = balanceConfig.getDamage("water_blast", 1);
        int damage5 = balanceConfig.getDamage("water_blast", 5);

        assertEquals(5, damage1, "Wasserstoß Level 1 sollte 5 Damage haben");
        assertEquals(13, damage5, "Wasserstoß Level 5 sollte 13 Damage haben");
    }

    @Test
    public void testDamageMultiplier() {
        balanceConfig.setDamageMultiplier(2.0);
        int damage = balanceConfig.getDamage("water_blast", 1);

        assertEquals(10, damage, "Wasserstoß mit 2x Multiplikator sollte 10 Damage haben");
    }

    @Test
    public void testDamageMultiplierClamping() {
        balanceConfig.setDamageMultiplier(10.0);
        assertEquals(5.0, balanceConfig.getDamageMultiplier(), "Multiplikator sollte auf 5.0 begrenzt sein");
    }

    @Test
    public void testCooldownValues() {
        int cooldown1 = balanceConfig.getCooldown("water_blast", 1);
        int cooldown5 = balanceConfig.getCooldown("water_blast", 5);

        assertEquals(5, cooldown1, "Wasserstoß Level 1 sollte 5s Cooldown haben");
        assertEquals(1, cooldown5, "Wasserstoß Level 5 sollte 1s Cooldown haben");
    }

    @Test
    public void testManaCosts() {
        int mana1 = balanceConfig.getManaCost("water_blast", 1);
        int mana5 = balanceConfig.getManaCost("water_blast", 5);

        assertEquals(20, mana1, "Wasserstoß Level 1 sollte 20 Mana kosten");
        assertEquals(40, mana5, "Wasserstoß Level 5 sollte 40 Mana kosten");
    }

    @Test
    public void testAllAbilitiesHaveValues() {
        String[] abilities = {"water_blast", "water_heal", "fire_burst", "fire_shield",
                "ice_spike", "ice_freeze", "lightning_strike", "lightning_chain",
                "earth_quake", "earth_wall"};

        for (String ability : abilities) {
            for (int level = 1; level <= 5; level++) {
                int damage = balanceConfig.getDamage(ability, level);
                int cooldown = balanceConfig.getCooldown(ability, level);
                int mana = balanceConfig.getManaCost(ability, level);

                assertTrue(damage > 0, ability + " Level " + level + " sollte Damage haben");
                assertTrue(cooldown > 0, ability + " Level " + level + " sollte Cooldown haben");
                assertTrue(mana > 0, ability + " Level " + level + " sollte Mana-Kosten haben");
            }
        }
    }

    @Test
    public void testFireExplosionDamageProgression() {
        int[] expectedDamages = {10, 13, 16, 19, 22};

        for (int level = 1; level <= 5; level++) {
            int damage = balanceConfig.getDamage("fire_burst", level);
            assertEquals(expectedDamages[level - 1], damage,
                    "Feuerexplosion Level " + level + " sollte " + expectedDamages[level - 1] + " Damage haben");
        }
    }

    @Test
    public void testLightningStrikeDamageProgression() {
        int[] expectedDamages = {12, 15, 18, 21, 24};

        for (int level = 1; level <= 5; level++) {
            int damage = balanceConfig.getDamage("lightning_strike", level);
            assertEquals(expectedDamages[level - 1], damage,
                    "Blitzschlag Level " + level + " sollte " + expectedDamages[level - 1] + " Damage haben");
        }
    }

    @Test
    public void testInvalidAbilityHandling() {
        int damage = balanceConfig.getDamage("invalid_ability", 1);
        assertEquals(0, damage, "Ungültige Fähigkeit sollte 0 Damage zurückgeben");

        int cooldown = balanceConfig.getCooldown("invalid_ability", 1);
        assertEquals(0, cooldown, "Ungültige Fähigkeit sollte 0 Cooldown zurückgeben");
    }

    @Test
    public void testInvalidLevelHandling() {
        int damage = balanceConfig.getDamage("water_blast", 10);
        assertEquals(0, damage, "Ungültiges Level sollte 0 Damage zurückgeben");

        int cooldown = balanceConfig.getCooldown("water_blast", 0);
        assertEquals(0, cooldown, "Level 0 sollte 0 Cooldown zurückgeben");
    }

    @Test
    public void testBalanceConfigMultipliers() {
        balanceConfig.setDamageMultiplier(1.5);
        balanceConfig.setCooldownMultiplier(0.8);
        balanceConfig.setManaCostMultiplier(0.9);

        assertEquals(1.5, balanceConfig.getDamageMultiplier());
        assertEquals(0.8, balanceConfig.getCooldownMultiplier());
        assertEquals(0.9, balanceConfig.getManaCostMultiplier());
    }

    @Test
    public void testCooldownMultiplier() {
        balanceConfig.setCooldownMultiplier(0.5);
        int cooldown = balanceConfig.getCooldown("water_blast", 1);

        assertEquals(2, cooldown, "Wasserstoß mit 0.5x Multiplikator sollte 2.5s Cooldown haben (gerundet)");
    }

    @Test
    public void testManaCostMultiplier() {
        balanceConfig.setManaCostMultiplier(0.5);
        int mana = balanceConfig.getManaCost("water_blast", 1);

        assertEquals(10, mana, "Wasserstoß mit 0.5x Multiplikator sollte 10 Mana kosten");
    }

    @Test
    public void testEarthquakeDamage() {
        int damage1 = balanceConfig.getDamage("earth_quake", 1);
        int damage5 = balanceConfig.getDamage("earth_quake", 5);

        assertEquals(15, damage1, "Erdbeben Level 1 sollte 15 Damage haben");
        assertEquals(27, damage5, "Erdbeben Level 5 sollte 27 Damage haben");
    }

    @Test
    public void testBalanceReport() {
        String report = balanceConfig.getBalanceReport();

        assertNotNull(report);
        assertTrue(report.contains("BALANCING REPORT"));
        assertTrue(report.contains("Damage Multiplier"));
        assertTrue(report.contains("Cooldown Multiplier"));
    }
}
