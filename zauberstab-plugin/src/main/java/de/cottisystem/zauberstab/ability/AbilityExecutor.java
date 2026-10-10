package de.cottisystem.zauberstab.ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import de.cottisystem.zauberstab.ZauberstabPlugin;
import de.cottisystem.zauberstab.combination.CombinationEngine;
import de.cottisystem.zauberstab.combination.CombinationResult;
import de.cottisystem.zauberstab.cooldown.CooldownDisplay;
import de.cottisystem.zauberstab.mana.ManaDisplay;
import de.cottisystem.zauberstab.model.Ability;
import de.cottisystem.zauberstab.model.Staff;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Führt Fähigkeiten aus: prüft Mana und Cooldowns, verbraucht Mana,
 * wendet Schaden/Effekte an und spielt Animationen ab.
 *
 * Nutzt die CombinationEngine: Die Tags aller Fähigkeiten auf Main- und
 * Offhand-Stab bestimmen Boni (Schaden, Reichweite, AoE) sowie
 * Trank-Effekte (Brand, Verlangsamung, Betäubung, Regeneration).
 */
public class AbilityExecutor {

    private static final double DEFAULT_RANGE = 15.0;
    private static final double DEFAULT_AOE_RADIUS = 4.0;
    private static final int WALL_DURATION_TICKS = 100; // 5 Sekunden
    private static final int XP_PER_CAST = 10;
    private static final int BOOST_PERCENT_PER_SLOT = 10;
    private static final int BOOST_ELEMENT_MATCH_PERCENT = 15;
    private static final long KILL_TRACK_TIMEOUT_MS = 10_000;

    private final ZauberstabPlugin plugin;

    // Merkt sich den zuletzt gewirkten Fähigkeits-Treffer pro Opfer (für Todesnachrichten)
    private final Map<UUID, AbilityHit> lastAbilityHits = new ConcurrentHashMap<>();

    // Der Stab, mit dem gerade gezaubert wird (für Verstärkungs-Slots)
    private Staff activeStaff;

    /**
     * Letzter Fähigkeits-Treffer auf ein Ziel (Zaubernder, Fähigkeit, Zeitpunkt).
     */
    public record AbilityHit(String casterName, String abilityName, long timestamp) {

        public boolean isRecent() {
            return System.currentTimeMillis() - timestamp <= KILL_TRACK_TIMEOUT_MS;
        }
    }

    public AbilityExecutor(ZauberstabPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Führt eine Fähigkeit für einen Spieler aus.
     *
     * @return true, wenn die Fähigkeit erfolgreich ausgeführt wurde
     */
    public boolean execute(Player player, Staff staff, Ability ability) {
        int level = getAbilityLevel(player, ability);
        this.activeStaff = staff;

        if (level <= 0) {
            player.sendMessage("§cFähigkeit §6" + ability.getDisplayName() + " §cnoch nicht freigeschaltet!");
            return false;
        }

        // Globaler Cooldown (Spam-Schutz)
        if (plugin.getCooldownManager().isOnGlobalCooldown(player)) {
            return false;
        }

        // Fähigkeits-Cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, ability)) {
            int remaining = plugin.getCooldownManager().getRemainingCooldown(player, ability);
            CooldownDisplay.sendCooldownMessage(player, ability, remaining);
            return false;
        }

        // Mana prüfen
        int manaCost = plugin.getBalanceManager().getFinalManaCost(ability, level);
        int currentMana = plugin.getManaManager().getMana(player);
        if (currentMana < manaCost) {
            player.sendMessage("§cNicht genug Mana! §7(" + currentMana + "/" + manaCost + ")");
            plugin.getAnimationManager().playErrorEffect(player);
            return false;
        }

        // Kombination aus Main- und Offhand-Stab berechnen
        Staff offhandStaff = plugin.getStaffManager().getPlayerOffhandStaff(player).orElse(null);
        CombinationResult combo = CombinationEngine.calculateCombination(staff, offhandStaff, false);

        // Effekt ausführen
        boolean success = executeEffect(player, ability, level, combo);
        if (!success) {
            player.sendMessage("§7Kein Ziel gefunden.");
            return false;
        }

        // Mana verbrauchen und Cooldowns setzen
        plugin.getManaManager().setMana(player, currentMana - manaCost);
        int cooldown = plugin.getBalanceManager().getFinalCooldown(ability, level);
        plugin.getCooldownManager().setCooldown(player, ability, cooldown);
        plugin.getCooldownManager().setGlobalCooldown(player);

        // Kombinations-Support-Effekte auf den Zaubernden anwenden (Tränke)
        applyComboSupportEffects(player, ability, combo);

        // Cast-Animation und Mana-Anzeige
        plugin.getAnimationManager().playCastEffect(player, ability);
        ManaDisplay.displayManaBar(player, plugin.getManaManager());

        // Zeige aktive Kombination an
        if (combo.getActiveEffectCount() > 0) {
            player.sendMessage("§d⚗ Kombo: §7" + combo.getDescription());
        }

        // Erfahrung für erfolgreiches Zaubern vergeben (Level-Up gibt Skill-Punkte)
        var playerSkillTree = plugin.getSkillTree().getOrCreatePlayerSkillTree(player.getUniqueId());
        if (playerSkillTree.addExperience(XP_PER_CAST)) {
            player.sendMessage("§6✨ Level-Up! Du bist jetzt Level §e" + playerSkillTree.getLevel()
                    + "§6! §7(+1 Skill-Punkt)");
        }

        return true;
    }

    /**
     * Ermittelt das Fähigkeits-Level aus dem Skill-Tree des Spielers.
     * Nicht freigeschaltete Fähigkeiten geben 0 zurück.
     */
    private int getAbilityLevel(Player player, Ability ability) {
        var playerSkillTree = plugin.getSkillTree().getOrCreatePlayerSkillTree(player.getUniqueId());
        int level = playerSkillTree.getAbilityLevel(ability.getId() + "_node");
        return Math.min(level, ability.getMaxLevel());
    }

    /**
     * Führt den eigentlichen Effekt einer Fähigkeit aus.
     */
    private boolean executeEffect(Player player, Ability ability, int level, CombinationResult combo) {
        return switch (ability.getId()) {
            case "water_blast" -> castWaterBlast(player, ability, level, combo);
            case "water_heal" -> castWaterHeal(player, ability, level, combo);
            case "water_wave" -> castWaterWave(player, ability, level, combo);
            case "water_breath" -> castWaterBreath(player, ability, level);
            case "water_geyser" -> castWaterGeyser(player, ability, level, combo);
            case "water_bubble" -> castWaterBubble(player, ability, level);
            case "fire_burst" -> castFireBurst(player, ability, level, combo);
            case "fire_shield" -> castFireShield(player, ability, level);
            case "fire_wall" -> castFireWall(player, ability, level, combo);
            case "fire_storm" -> castFireStorm(player, ability, level, combo);
            case "fire_spread" -> castFireSpread(player, ability, level, combo);
            case "fire_phoenix" -> castFirePhoenix(player, ability, level);
            case "ice_spike" -> castIceSpike(player, ability, level, combo);
            case "ice_freeze" -> castIceFreeze(player, ability, level, combo);
            case "ice_armor" -> castIceArmor(player, ability, level);
            case "ice_blizzard" -> castIceBlizzard(player, ability, level, combo);
            case "ice_lance" -> castIceLance(player, ability, level, combo);
            case "ice_shard" -> castIceShard(player, ability, level, combo);
            case "lightning_strike" -> castLightningStrike(player, ability, level, combo);
            case "lightning_chain" -> castLightningChain(player, ability, level, combo);
            case "lightning_speed" -> castLightningSpeed(player, ability, level);
            case "lightning_shield" -> castLightningShield(player, ability, level);
            case "lightning_dash" -> castLightningDash(player, ability, level);
            case "lightning_orb" -> castLightningOrb(player, ability, level, combo);
            case "earth_quake" -> castEarthQuake(player, ability, level, combo);
            case "earth_wall" -> castEarthWall(player);
            case "earth_armor" -> castEarthArmor(player, ability, level);
            case "earth_fist" -> castEarthFist(player, ability, level, combo);
            case "earth_pillar" -> castEarthPillar(player, ability, level, combo);
            case "earth_quicksand" -> castEarthQuicksand(player, ability, level, combo);
            case "mana_shield" -> castManaShield(player, ability, level);
            case "mana_barrier" -> castManaBarrier(player, ability, level);
            case "mana_infusion" -> castManaInfusion(player, ability, level);
            case "shadow_cloak" -> castShadowCloak(player, ability, level);
            case "shadow_mark" -> castShadowMark(player, ability, level, combo);
            case "soul_drain" -> castSoulDrain(player, ability, level, combo);
            case "shadow_step" -> castShadowStep(player, ability, level);
            case "dark_void" -> castDarkVoid(player, ability, level, combo);
            case "shadow_clone" -> castShadowClone(player, ability, level);
            case "max_mana", "mana_regen", "mana_mastery" -> castPassiveMessage(player, ability);
            default -> castGeneric(player, ability, level, combo);
        };
    }

    // ===== EINZELNE FÄHIGKEITEN =====

    /** Wasserstoß: Einzelziel-Schaden mit Knockback. */
    private boolean castWaterBlast(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity target = findTarget(player, getRange(combo));
        if (target == null) {
            return false;
        }

        damage(player, target, ability, level, combo);
        knockback(player, target);
        plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        return true;
    }

    /** Wasserheilung: Heilt den Spieler (Kombo-Bonus: +% Max-HP). */
    private boolean castWaterHeal(Player player, Ability ability, int level, CombinationResult combo) {
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double healAmount = plugin.getBalanceManager().getFinalDamage(ability, level, player)
                * plugin.getBalanceManager().getConfig().getHealingMultiplier();

        // Verstärkungs-Slots (3+) erhöhen auch die Heilung
        healAmount = healAmount * (100 + getSupportBoostPercent(ability)) / 100.0;

        // Kombinations-Bonus: zusätzliche Heilung in % der Max-HP
        if (combo.getHealAmount() > 0) {
            healAmount += maxHealth * combo.getHealAmount() / 100.0;
        }

        if (player.getHealth() >= maxHealth) {
            player.sendMessage("§7Du bist bereits vollständig geheilt.");
            return false;
        }

        player.setHealth(Math.min(player.getHealth() + healAmount, maxHealth));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§a+" + (int) healAmount + " Leben");
        return true;
    }

    /** Feuerexplosion: Flächenschaden mit Brandeffekt. */
    private boolean castFireBurst(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, 0));
        if (targets.isEmpty()) {
            return false;
        }

        for (LivingEntity target : targets) {
            damage(player, target, ability, level, combo);
            target.setFireTicks(40 + level * 20); // 2s + 1s pro Level
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        }
        return true;
    }

    /** Feuerschild: Feuerresistenz und Absorption. */
    private boolean castFireShield(Player player, Ability ability, int level) {
        int durationTicks = (100 + level * 40); // 5s + 2s pro Level
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, durationTicks, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, durationTicks, Math.min(level - 1, 2)));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§6Feuerschild aktiv für §e" + (durationTicks / 20) + "s§6!");
        return true;
    }

    /** Eisspeer: Einzelziel-Schaden mit Verlangsamung. */
    private boolean castIceSpike(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity target = findTarget(player, getRange(combo));
        if (target == null) {
            return false;
        }

        damage(player, target, ability, level, combo);
        applySlowness(target, level);
        plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        return true;
    }

    /** Eisfrost: Verlangsamt alle Gegner in der Nähe. */
    private boolean castIceFreeze(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, level));
        if (targets.isEmpty()) {
            return false;
        }

        for (LivingEntity target : targets) {
            damage(player, target, ability, level, combo);
            applySlowness(target, level + 1);
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        }
        return true;
    }

    /** Blitzschlag: Blitz auf das anvisierte Ziel. */
    private boolean castLightningStrike(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity target = findTarget(player, getRange(combo));
        if (target == null) {
            return false;
        }

        target.getWorld().strikeLightningEffect(target.getLocation());
        damage(player, target, ability, level, combo);
        plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        return true;
    }

    /** Blitzkette: Blitz springt zwischen bis zu (2 + Level) Gegnern. */
    private boolean castLightningChain(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity first = findTarget(player, getRange(combo));
        if (first == null) {
            return false;
        }

        int maxJumps = 2 + level;
        Set<LivingEntity> hit = new HashSet<>();
        LivingEntity current = first;

        while (current != null && hit.size() < maxJumps) {
            hit.add(current);
            current.getWorld().strikeLightningEffect(current.getLocation());
            damage(player, current, ability, level, combo);
            plugin.getAnimationManager().playHitEffect(current.getLocation(), ability);
            current = findChainTarget(player, current, hit);
        }
        return true;
    }

    /** Erdbeben: Flächenschaden mit Hochschleudern. */
    private boolean castEarthQuake(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, 1));
        if (targets.isEmpty()) {
            return false;
        }

        for (LivingEntity target : targets) {
            damage(player, target, ability, level, combo);
            target.setVelocity(target.getVelocity().setY(0.5 + level * 0.1));
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        }
        return true;
    }

    /** Erdwall: Temporäre Schutzwand vor dem Spieler. */
    private boolean castEarthWall(Player player) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        Location base = player.getLocation().add(direction.clone().multiply(2)).getBlock().getLocation();

        // Senkrechte zur Blickrichtung (für 3 Blöcke Breite)
        Vector side = new Vector(-direction.getZ(), 0, direction.getX());

        List<Block> placedBlocks = new ArrayList<>();
        for (int w = -1; w <= 1; w++) {
            for (int h = 0; h < 2; h++) {
                Block block = base.clone()
                        .add(side.clone().multiply(w))
                        .add(0, h, 0)
                        .getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.DIRT);
                    placedBlocks.add(block);
                }
            }
        }

        if (placedBlocks.isEmpty()) {
            return false;
        }

        // Wand nach Ablauf der Zeit wieder entfernen
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Block block : placedBlocks) {
                if (block.getType() == Material.DIRT) {
                    block.setType(Material.AIR);
                }
            }
        }, WALL_DURATION_TICKS);

        player.sendMessage("§6Erdwall errichtet! §7(" + (WALL_DURATION_TICKS / 20) + "s)");
        return true;
    }

    /** Schattenmantel: Spieler wird kurzzeitig unsichtbar. */
    private boolean castShadowCloak(Player player, Ability ability, int level) {
        int durationTicks = (100 + level * 40); // 5s + 2s pro Level
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 0, false, false));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§8Schattenmantel aktiv für §7" + (durationTicks / 20) + "s§8!");
        return true;
    }

    /** Mal der Verdammnis: Markiert ein Ziel (leuchtet und nimmt mehr Schaden). */
    private boolean castShadowMark(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity target = findTarget(player, getRange(combo));
        if (target == null) {
            return false;
        }

        int durationTicks = (60 + level * 40); // 3s + 2s pro Level
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, durationTicks, 0, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, Math.min(level - 1, 2), false, false));
        damage(player, target, ability, level, combo);
        plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        player.sendMessage("§8Du hast §7" + target.getName() + " §8markiert!");
        return true;
    }

    /** Seelenentzug: Schaden am Ziel, Heilung für den Zaubernden. */
    private boolean castSoulDrain(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity target = findTarget(player, getRange(combo));
        if (target == null) {
            return false;
        }

        damage(player, target, ability, level, combo);
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double heal = Math.min(maxHealth - player.getHealth(), (ability.getBasePower() + level * 2) * (100 + getSupportBoostPercent(ability)) / 100.0);
        player.setHealth(Math.min(player.getHealth() + heal, maxHealth));
        plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        player.sendMessage("§8Seelenentzug: §7" + (int) heal + "§8 Leben geheilt");
        return true;
    }

    /** Wasserwelle: Stoßt nahe Gegner zurück. */
    private boolean castWaterWave(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, 0));
        if (targets.isEmpty()) {
            return false;
        }

        for (LivingEntity target : targets) {
            damage(player, target, ability, level, combo);
            knockback(player, target);
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        }
        return true;
    }

    /** Wasseratem: Unterwasseratem und Geschwindigkeit. */
    private boolean castWaterBreath(Player player, Ability ability, int level) {
        int durationTicks = (200 + level * 60); // 10s + 3s pro Level
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, durationTicks, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, durationTicks, 0));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§bWasseratem aktiv für §3" + (durationTicks / 20) + "s§b!");
        return true;
    }

    /** Feuerwand: Feuer-Fläche vor dem Spieler. */
    private boolean castFireWall(Player player, Ability ability, int level, CombinationResult combo) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        Location base = player.getLocation().add(direction.clone().multiply(3));

        List<Block> placed = new ArrayList<>();
        Vector side = new Vector(-direction.getZ(), 0, direction.getX());
        for (int w = -2; w <= 2; w++) {
            Block block = base.clone().add(side.clone().multiply(w)).getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.FIRE);
                placed.add(block);
            }
        }

        if (placed.isEmpty()) {
            return false;
        }

        // Feuer nach kurzer Zeit löschen
        int duration = Math.max(40, (level * 20));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Block block : placed) {
                if (block.getType() == Material.FIRE) {
                    block.setType(Material.AIR);
                }
            }
        }, duration);

        player.sendMessage("§cFeuerwand errichtet!");
        return true;
    }

    /** Feuersturm: Große Fläche mit Feuerbällen. */
    private boolean castFireStorm(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, 2));
        if (targets.isEmpty()) {
            return false;
        }

        for (LivingEntity target : targets) {
            damage(player, target, ability, level, combo);
            target.setFireTicks(60 + level * 30);
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        }
        player.sendMessage("§cFeuersturm!");
        return true;
    }

    /** Eispanzer: Reduziert Schaden durch Resistenz. */
    private boolean castIceArmor(Player player, Ability ability, int level) {
        int durationTicks = (160 + level * 40);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, Math.min(level - 1, 2)));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 0));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§fEispanzer aktiv für §7" + (durationTicks / 20) + "s§f!");
        return true;
    }

    /** Blizzard: Starke Kälte-Flächenattacke. */
    private boolean castIceBlizzard(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, 1));
        if (targets.isEmpty()) {
            return false;
        }

        for (LivingEntity target : targets) {
            damage(player, target, ability, level, combo);
            applySlowness(target, level + 2);
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        }
        player.sendMessage("§fBlizzard!");
        return true;
    }

    /** Blitzgeschwindigkeit: Schnelligkeit. */
    private boolean castLightningSpeed(Player player, Ability ability, int level) {
        int durationTicks = (160 + level * 40);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, Math.min(level, 2)));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§eBlitzgeschwindigkeit aktiv für §6" + (durationTicks / 20) + "s§e!");
        return true;
    }

    /** Blitzschild: Schlägt Gegner in der Nähe mit Blitz zurück. */
    private boolean castLightningShield(Player player, Ability ability, int level) {
        List<LivingEntity> targets = findNearbyEnemies(player, 4.0);
        if (targets.isEmpty()) {
            player.sendMessage("§7Kein Angreifer in Reichweite.");
            return false;
        }

        for (LivingEntity target : targets) {
            target.getWorld().strikeLightningEffect(target.getLocation());
            target.damage(level * 3, player);
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        }
        player.sendMessage("§eBlitzschild!");
        return true;
    }

    /** Erdrüstung: Erhöht Rüstung und Absorption. */
    private boolean castEarthArmor(Player player, Ability ability, int level) {
        int durationTicks = (200 + level * 40);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, Math.min(level - 1, 2)));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, durationTicks, Math.min(level - 1, 2)));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§2Erdrüstung aktiv für §a" + (durationTicks / 20) + "s§2!");
        return true;
    }

    /** Erdfaust: Einzelziel-Schlag mit Stun. */
    private boolean castEarthFist(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity target = findTarget(player, getRange(combo));
        if (target == null) {
            return false;
        }

        damage(player, target, ability, level, combo);
        int stunDuration = (20 + level * 10);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stunDuration, 3));
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, stunDuration, 0));
        target.setVelocity(target.getVelocity().setY(0.6));
        plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        return true;
    }

    /** Mana-Schild: Mana-Überzug, der Schaden reduziert. */
    private boolean castManaShield(Player player, Ability ability, int level) {
        int durationTicks = (160 + level * 40);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, Math.min(level, 2)));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§9Mana-Schild aktiv für §1" + (durationTicks / 20) + "s§9!");
        return true;
    }

    /** Schritt der Schatten: Teleport nach vorne. */
    private boolean castShadowStep(Player player, Ability ability, int level) {
        Vector direction = player.getLocation().getDirection().normalize();
        int distance = 4 + level;
        Location target = player.getLocation().add(direction.multiply(distance));
        target.setY(Math.ceil(target.getY()));
        player.teleport(target);
        plugin.getAnimationManager().playHitEffect(player.getLocation(), ability);
        player.sendMessage("§5Du bist in die Schatten getreten!");
        return true;
    }

    /** Wassergeysir: Schadet und schleudert Ziele in die Luft. */
    private boolean castWaterGeyser(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity target = findTarget(player, getRange(combo));
        if (target == null) {
            return false;
        }

        damage(player, target, ability, level, combo);
        target.setVelocity(target.getVelocity().setY(1.2 + level * 0.15));
        plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        return true;
    }

    /** Wasserblase: Schützt mit Resistenz und Wasseratmung. */
    private boolean castWaterBubble(Player player, Ability ability, int level) {
        int durationTicks = (200 + level * 60);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, Math.min(level - 1, 2)));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, durationTicks, 0));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§bWasserblase aktiv für §3" + (durationTicks / 20) + "s§b!");
        return true;
    }

    /** Feuerausbreitung: Zündet Gegner in der Nähe und verursacht Schaden. */
    private boolean castFireSpread(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, 1));
        if (targets.isEmpty()) {
            return false;
        }

        for (LivingEntity target : targets) {
            damage(player, target, ability, level, combo);
            target.setFireTicks(80 + level * 20);
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        }
        player.sendMessage("§cFlammen verbreiten sich!");
        return true;
    }

    /** Phönix: Kurzzeitige Unverwundbarkeit. */
    private boolean castFirePhoenix(Player player, Ability ability, int level) {
        int durationTicks = (60 + level * 40);
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, durationTicks, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, Math.min(level, 2)));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§6Phönix-Feuer für §e" + (durationTicks / 20) + "s§6!");
        return true;
    }

    /** Eislanze: Hoher Durchschlagsschaden am Ziel. */
    private boolean castIceLance(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity target = findTarget(player, getRange(combo));
        if (target == null) {
            return false;
        }

        damage(player, target, ability, level, combo);
        applySlowness(target, level + 1);
        target.getWorld().playEffect(target.getLocation(), org.bukkit.Effect.STEP_SOUND, org.bukkit.Material.ICE);
        plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        return true;
    }

    /** Eissplitter: Mehrere Ziele in Reichweite treffen. */
    private boolean castIceShard(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, 1));
        if (targets.isEmpty()) {
            return false;
        }

        int hit = 0;
        for (LivingEntity target : targets) {
            if (hit >= 2 + level) break;
            damage(player, target, ability, level, combo);
            applySlowness(target, level);
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
            hit++;
        }
        return true;
    }

    /** Blitz-Dash: Schnelle Bewegung in Blickrichtung. */
    private boolean castLightningDash(Player player, Ability ability, int level) {
        Vector direction = player.getLocation().getDirection().normalize();
        int distance = 5 + level;
        Location target = player.getLocation().add(direction.multiply(distance));
        player.teleport(target);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40 + level * 20, 1));
        plugin.getAnimationManager().playHitEffect(player.getLocation(), ability);
        player.sendMessage("§eBlitz-Dash!");
        return true;
    }

    /** Blitzkugel: Explosiver Kugelblitz in der Nähe. */
    private boolean castLightningOrb(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, 1));
        if (targets.isEmpty()) {
            return false;
        }

        for (LivingEntity target : targets) {
            target.getWorld().strikeLightningEffect(target.getLocation());
            damage(player, target, ability, level, combo);
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        }
        return true;
    }

    /** Erdpfeiler: Schadet und hebt ein Ziel an. */
    private boolean castEarthPillar(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity target = findTarget(player, getRange(combo));
        if (target == null) {
            return false;
        }

        damage(player, target, ability, level, combo);
        target.setVelocity(target.getVelocity().setY(0.8 + level * 0.2));
        plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        return true;
    }

    /** Treibsand: Verlangsamt Gegner und zieht sie langsam in den Boden. */
    private boolean castEarthQuicksand(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, 1));
        if (targets.isEmpty()) {
            return false;
        }

        for (LivingEntity target : targets) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60 + level * 40, 2));
            damage(player, target, ability, level, combo);
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);

            new org.bukkit.scheduler.BukkitRunnable() {
                private int runs = 0;

                @Override
                public void run() {
                    if (target.isDead() || !target.isValid() || runs >= 10) {
                        cancel();
                        return;
                    }
                    Location loc = target.getLocation();
                    double sink = 0.08 + level * 0.04;
                    target.teleport(loc.add(0, -sink, 0));
                    runs++;
                }
            }.runTaskTimer(plugin, 0L, 4L);
        }
        return true;
    }

    /** Mana-Barriere: Resistenz und Manaregen-Boost. */
    private boolean castManaBarrier(Player player, Ability ability, int level) {
        int durationTicks = (120 + level * 40);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, Math.min(level - 1, 2)));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, durationTicks, 0));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§9Mana-Barriere aktiv für §1" + (durationTicks / 20) + "s§9!");
        return true;
    }

    /** Mana-Infusion: Stellt sofort Mana wieder her. */
    private boolean castManaInfusion(Player player, Ability ability, int level) {
        int currentMana = plugin.getManaManager().getMana(player);
        int maxMana = plugin.getManaManager().getMaxMana(player);
        int restore = (maxMana / 10) * level;
        plugin.getManaManager().setMana(player, Math.min(currentMana + restore, maxMana));
        plugin.getAnimationManager().playHitEffect(player.getLocation().add(0, 1, 0), ability);
        player.sendMessage("§9Mana-Infusion: §1" + restore + " §9Mana wiederhergestellt!");
        return true;
    }

    /** Dunkle Leere: Schaden und Blindheit im Umkreis. */
    private boolean castDarkVoid(Player player, Ability ability, int level, CombinationResult combo) {
        List<LivingEntity> targets = findNearbyEnemies(player, getAoeRadius(combo, 1));
        if (targets.isEmpty()) {
            return false;
        }

        for (LivingEntity target : targets) {
            damage(player, target, ability, level, combo);
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40 + level * 20, 0));
            plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        }
        return true;
    }

    /** Schatten-Klon: Erstellt Klone des Spielers, die wie er aussehen. */
    private boolean castShadowClone(Player player, Ability ability, int level) {
        int cloneCount = Math.min(level, 3);
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();

        for (int i = 0; i < cloneCount; i++) {
            org.bukkit.entity.Entity entity = player.getWorld().spawnEntity(player.getLocation(), org.bukkit.entity.EntityType.ZOMBIE);
            if (!(entity instanceof org.bukkit.entity.Zombie clone)) {
                continue;
            }

            clone.setCustomName(player.getName());
            clone.setCustomNameVisible(true);
            clone.setAdult();
            clone.setCanBreakDoors(false);
            clone.setAware(false);
            clone.setTarget(null);

            org.bukkit.inventory.EntityEquipment equipment = clone.getEquipment();
            if (equipment != null) {
                equipment.setHelmet(inv.getHelmet());
                equipment.setChestplate(inv.getChestplate());
                equipment.setLeggings(inv.getLeggings());
                equipment.setBoots(inv.getBoots());
                equipment.setItemInMainHand(inv.getItemInMainHand());
                equipment.setItemInOffHand(inv.getItemInOffHand());
            }

            plugin.getAnimationManager().playHitEffect(clone.getLocation(), ability);
        }

        player.sendMessage("§5Schatten-Klon erschaffen!");
        return true;
    }

    /** Passive Skills: Melden, dass sie nicht aktiv gewirkt werden können. */
    private boolean castPassiveMessage(Player player, Ability ability) {
        player.sendMessage("§7" + ability.getDisplayName() + " ist passiv und kann nicht gewirkt werden.");
        return false;
    }

    /** Fallback für unbekannte Fähigkeiten: Einzelziel-Schaden. */
    private boolean castGeneric(Player player, Ability ability, int level, CombinationResult combo) {
        LivingEntity target = findTarget(player, getRange(combo));
        if (target == null) {
            return false;
        }

        damage(player, target, ability, level, combo);
        plugin.getAnimationManager().playHitEffect(target.getLocation(), ability);
        return true;
    }

    // ===== KOMBINATIONS-EFFEKTE =====

    /**
     * Gibt die effektive Reichweite zurück (Kombo kann sie erhöhen).
     */
    private double getRange(CombinationResult combo) {
        return Math.max(DEFAULT_RANGE, combo.getRange());
    }

    /**
     * Gibt den effektiven AoE-Radius zurück (Kombo kann ihn erhöhen).
     */
    private double getAoeRadius(CombinationResult combo, int levelBonus) {
        return Math.max(DEFAULT_AOE_RADIUS + levelBonus, combo.getAoeRadius());
    }

    /**
     * Wendet Trank-Effekte aus der Kombination auf ein getroffenes Ziel an.
     * Tags: burn → Brand, slow/cc → Verlangsamung, stun → Verlangsamung IV + Blindheit.
     */
    private void applyComboDebuffs(LivingEntity target, CombinationResult combo) {
        int durationSeconds = combo.getDebuffDuration();
        if (durationSeconds <= 0 || combo.getTags() == null) {
            return;
        }

        int durationTicks = durationSeconds * 20;
        Set<String> tags = combo.getTags();

        if (tags.contains("burn")) {
            target.setFireTicks(Math.max(target.getFireTicks(), durationTicks));
        }
        if (tags.contains("slow") || tags.contains("cc")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 1));
        }
        if (tags.contains("stun")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks / 2, 3));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks / 2, 0));
        }
    }

    /**
     * Wendet unterstützende Trank-Effekte aus der Kombination auf den Zaubernden an.
     * Tags: heal → Regeneration, defense → Resistenz, support → Geschwindigkeit.
     */
    private void applyComboSupportEffects(Player player, Ability ability, CombinationResult combo) {
        if (combo.getTags() == null) {
            return;
        }

        Set<String> tags = combo.getTags();
        int durationTicks = Math.max(combo.getDebuffDuration(), 3) * 20;

        // Nur anwenden, wenn die gewirkte Fähigkeit selbst ein Support-/Defensiv-Tag hat
        boolean isSupportCast = ability.hasTag("support") || ability.hasTag("defense") || ability.hasTag("heal");
        if (!isSupportCast) {
            return;
        }

        if (tags.contains("heal")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, durationTicks, 0));
        }
        if (tags.contains("defense")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, 0));
        }
        if (tags.contains("support")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 0));
        }
    }

    // ===== HILFSMETHODEN =====

    /**
     * Fügt einem Ziel Schaden zu (Balance-Werte + Kombinations-Boni + PvP/PvE-Caps)
     * und wendet Kombo-Trank-Effekte an.
     */
    private void damage(Player caster, LivingEntity target, Ability ability, int level, CombinationResult combo) {
        int baseDamage = plugin.getBalanceManager().getFinalDamage(ability, level, target);

        // Kombinations-Boni und Verstärkungs-Slots (in %) anwenden
        int bonusPercent = combo.getDamageBonus() + combo.getElementBonus() + getSupportBoostPercent(ability);
        int totalDamage = baseDamage * (100 + bonusPercent) / 100;

        // Nach Bonus erneut cappen (PvP/PvE)
        int cap = target instanceof Player
                ? plugin.getBalanceManager().getConfig().getMaxPlayerDamage()
                : plugin.getBalanceManager().getConfig().getMaxMobDamage();
        totalDamage = Math.min(totalDamage, cap);

        target.damage(totalDamage, caster);

        // Treffer für Todesnachrichten merken
        lastAbilityHits.put(target.getUniqueId(),
                new AbilityHit(caster.getName(), ability.getDisplayName(), System.currentTimeMillis()));

        // Trank-Effekte aus der Kombination anwenden
        applyComboDebuffs(target, combo);
    }

    /**
     * Gibt den letzten Fähigkeits-Treffer auf ein Ziel zurück (oder null).
     * Wird für Todesnachrichten verwendet.
     */
    public AbilityHit getLastAbilityHit(UUID victimId) {
        AbilityHit hit = lastAbilityHits.get(victimId);
        if (hit == null || !hit.isRecent()) {
            lastAbilityHits.remove(victimId);
            return null;
        }
        return hit;
    }

    /**
     * Berechnet den Verstärkungs-Bonus aus den Slots 3+ des aktiven Stabs.
     * Jede Fähigkeit in einem Verstärkungs-Slot gibt +10% (bei gleichem Element +15%).
     */
    private int getSupportBoostPercent(Ability castAbility) {
        if (activeStaff == null) {
            return 0;
        }

        List<Ability> abilities = activeStaff.getAbilities();
        int boost = 0;
        for (int i = 2; i < abilities.size(); i++) {
            Ability support = abilities.get(i);
            if (support == null) {
                continue;
            }
            if (support.getElement() != null && castAbility.getElement() != null
                    && support.getElement() == castAbility.getElement()) {
                boost += BOOST_ELEMENT_MATCH_PERCENT;
            } else {
                boost += BOOST_PERCENT_PER_SLOT;
            }
        }
        return boost;
    }

    /**
     * Stößt ein Ziel vom Spieler weg.
     */
    private void knockback(Player caster, LivingEntity target) {
        double force = plugin.getBalanceManager().getConfig().getKnockbackForce();
        Vector direction = target.getLocation().toVector()
                .subtract(caster.getLocation().toVector())
                .normalize()
                .multiply(force)
                .setY(0.3);
        target.setVelocity(direction);
    }

    /**
     * Wendet einen Slowness-Effekt an.
     */
    private void applySlowness(LivingEntity target, int level) {
        int duration = plugin.getBalanceManager().getConfig().getSlownessDuration();
        int amplifier = Math.min(plugin.getBalanceManager().getConfig().getSlownessLevel() + level - 1, 3);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, amplifier));
    }

    /**
     * Findet das anvisierte Ziel via Ray-Trace.
     */
    private LivingEntity findTarget(Player player, double range) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                1.0,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
        );

        if (result == null || !(result.getHitEntity() instanceof LivingEntity living)) {
            return null;
        }
        return living;
    }

    /**
     * Findet alle lebenden Gegner in einem Radius um den Spieler.
     */
    private List<LivingEntity> findNearbyEnemies(Player player, double radius) {
        List<LivingEntity> enemies = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living && !entity.equals(player)) {
                enemies.add(living);
            }
        }
        return enemies;
    }

    /**
     * Findet das nächste Kettenziel für die Blitzkette.
     */
    private LivingEntity findChainTarget(Player caster, LivingEntity from, Set<LivingEntity> alreadyHit) {
        return from.getNearbyEntities(6, 6, 6).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(living -> !living.equals(caster) && !alreadyHit.contains(living))
                .min(Comparator.comparingDouble(living ->
                        living.getLocation().distanceSquared(from.getLocation())))
                .orElse(null);
    }
}
