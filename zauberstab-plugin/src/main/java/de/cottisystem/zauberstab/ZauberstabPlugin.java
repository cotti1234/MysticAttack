package de.cottisystem.zauberstab;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.config.ConfigManager;
import de.cottisystem.zauberstab.config.AbilityLoader;
import de.cottisystem.zauberstab.db.DatabaseManager;
import de.cottisystem.zauberstab.item.StaffManager;
import de.cottisystem.zauberstab.cooldown.CooldownManager;
import de.cottisystem.zauberstab.mana.ManaManager;
import de.cottisystem.zauberstab.skilltree.SkillTree;
import de.cottisystem.zauberstab.animation.AnimationManager;
import de.cottisystem.zauberstab.balance.BalanceManager;
import de.cottisystem.zauberstab.ability.AbilityExecutor;
import de.cottisystem.zauberstab.listener.AbilityDeathListener;
import de.cottisystem.zauberstab.listener.PlayerJoinListener;
import de.cottisystem.zauberstab.listener.PlayerQuitListener;
import de.cottisystem.zauberstab.model.PlayerProfile;
import de.cottisystem.zauberstab.skilltree.PlayerSkillTree;

import org.bukkit.entity.Player;

import java.util.HashMap;
import de.cottisystem.zauberstab.listener.StaffClickListener;
import de.cottisystem.zauberstab.listener.StaffHoldListener;
import de.cottisystem.zauberstab.listener.InventoryClickListener;
import de.cottisystem.zauberstab.listener.ForgeBlockListener;
import de.cottisystem.zauberstab.listener.ManaRegenListener;
import de.cottisystem.zauberstab.command.ZauberstabCommand;

/**
 * Hauptklasse des Zauberstab-Plugins.
 * Verwaltet den Lifecycle des Plugins und initialisiert alle Subsysteme.
 */
public class ZauberstabPlugin extends JavaPlugin {

    private static ZauberstabPlugin instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private StaffManager staffManager;
    private AbilityLoader abilityLoader;
    private CooldownManager cooldownManager;
    private ManaManager manaManager;
    private SkillTree skillTree;
    private AnimationManager animationManager;
    private BalanceManager balanceManager;
    private AbilityExecutor abilityExecutor;

    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("========================================");
        getLogger().info("Zauberstab-Plugin wird geladen...");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("========================================");

        try {
            // Konfiguration laden
            configManager = new ConfigManager(this);
            configManager.loadConfig();
            getLogger().info("✓ Konfiguration geladen");

            // Datenbank-Layer initialisieren
            databaseManager = new DatabaseManager(this, configManager);
            getLogger().info("✓ Datenbank-Layer initialisiert");

            // Stab-Manager initialisieren
            staffManager = new StaffManager(this);
            getLogger().info("✓ Stab-Manager initialisiert");

            // Fähigkeits-Loader initialisieren
            abilityLoader = new AbilityLoader(this);
            abilityLoader.loadAbilities();
            getLogger().info("✓ Fähigkeits-Loader initialisiert");

            // Cooldown-Manager initialisieren
            cooldownManager = new CooldownManager();
            getLogger().info("✓ Cooldown-Manager initialisiert");

            // Mana-Manager initialisieren
            manaManager = new ManaManager();
            getLogger().info("✓ Mana-Manager initialisiert");

            // Skill-Tree initialisieren
            skillTree = new SkillTree();
            initializeSkillTree();
            getLogger().info("✓ Skill-Tree initialisiert");

            // Animation-Manager initialisieren
            animationManager = new AnimationManager(this);
            getLogger().info("✓ Animation-Manager initialisiert");

            // Balance-Manager initialisieren
            balanceManager = new BalanceManager(this);
            getLogger().info("✓ Balance-Manager initialisiert");

            // Fähigkeits-Executor initialisieren
            abilityExecutor = new AbilityExecutor(this);
            getLogger().info("✓ Fähigkeits-Executor initialisiert");

            // GUI-Keys initialisieren
            de.cottisystem.zauberstab.gui.AbilitySelectGUI.init(this);
            de.cottisystem.zauberstab.skilltree.SkillTreeGUI.init(this);

            // Listener registrieren
            registerListeners();
            getLogger().info("✓ Event-Listener registriert");

            // Befehle registrieren
            registerCommands();
            getLogger().info("✓ Befehle registriert");

            getLogger().info("========================================");
            getLogger().info("Zauberstab-Plugin erfolgreich geladen!");
            getLogger().info("========================================");
        } catch (Exception e) {
            getLogger().severe("Fehler beim Laden des Plugins:");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Zauberstab-Plugin wird entladen...");

        try {
            // Live-Daten aller Online-Spieler in die Profile übernehmen
            for (Player player : getServer().getOnlinePlayers()) {
                syncPlayerToProfile(player);
            }

            if (manaManager != null) {
                manaManager.clearAll();
                getLogger().info("✓ Mana-Cache geleert");
            }

            if (cooldownManager != null) {
                cooldownManager.clearAll();
                getLogger().info("✓ Cooldown-Cache geleert");
            }

            if (staffManager != null) {
                staffManager.clearCache();
                getLogger().info("✓ Stab-Cache geleert");
            }

            if (databaseManager != null) {
                databaseManager.shutdown();
                getLogger().info("✓ Datenbank-Verbindungen geschlossen");
            }
        } catch (Exception e) {
            getLogger().severe("Fehler beim Entladen des Plugins:");
            e.printStackTrace();
        }

        getLogger().info("Zauberstab-Plugin entladen.");
    }

    /**
     * Initialisiert den Skill-Tree mit vordefinierten Knoten.
     */
    private void initializeSkillTree() {
        // Branches / Hauptzweige definieren
        skillTree.addBranch(new de.cottisystem.zauberstab.skilltree.SkillTreeBranch(
                "water", "Wasser", "Kontrolliere das Wasser", Material.WATER_BUCKET, "§b"));
        skillTree.addBranch(new de.cottisystem.zauberstab.skilltree.SkillTreeBranch(
                "fire", "Feuer", "Beherrsche die Flammen", Material.LAVA_BUCKET, "§c"));
        skillTree.addBranch(new de.cottisystem.zauberstab.skilltree.SkillTreeBranch(
                "ice", "Eis", "Friere deine Feinde ein", Material.ICE, "§f"));
        skillTree.addBranch(new de.cottisystem.zauberstab.skilltree.SkillTreeBranch(
                "lightning", "Blitz", "Elektrische Zerstörung", Material.GLOWSTONE_DUST, "§e"));
        skillTree.addBranch(new de.cottisystem.zauberstab.skilltree.SkillTreeBranch(
                "earth", "Erde", "Die Kraft des Steins", Material.GRASS_BLOCK, "§2"));
        skillTree.addBranch(new de.cottisystem.zauberstab.skilltree.SkillTreeBranch(
                "mana", "Mana", "Mehr Mana und Kontrolle", Material.LAPIS_LAZULI, "§9"));
        skillTree.addBranch(new de.cottisystem.zauberstab.skilltree.SkillTreeBranch(
                "dark", "Verbotene Magie", "Dunkle, verbotene Kräfte", Material.DRAGON_BREATH, "§5"));

        // Wasser-Fähigkeiten
        skillTree.addNode("water", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "water_blast_node", "water_blast", "Wasserstoß", "Ein kraftvoller Wasserstoß",
                1, 2, 1, 5, null, 0, 0));
        skillTree.addNode("water", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "water_heal_node", "water_heal", "Wasserheilung", "Heilt mit Wasser-Energie",
                5, 3, 2, 5, new String[]{"water_blast_node"}, 0, 1));
        skillTree.addNode("water", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "water_wave_node", "water_wave", "Wasserwelle", "Stoßt mehrere Feinde zurück",
                10, 4, 2, 5, new String[]{"water_blast_node"}, 0, 2));
        skillTree.addNode("water", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "water_breath_node", "water_breath", "Wasseratem", "Unterwasseratem und Schnelligkeit",
                12, 4, 3, 5, new String[]{"water_heal_node"}, 0, 3));
        skillTree.addNode("water", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "water_geyser_node", "water_geyser", "Wassergeysir", "Schleudert Gegner in die Luft",
                18, 6, 3, 5, new String[]{"water_breath_node"}, 0, 4));
        skillTree.addNode("water", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "water_bubble_node", "water_bubble", "Wasserblase", "Schützende Wasserblase",
                22, 7, 3, 5, new String[]{"water_geyser_node"}, 0, 5));

        // Feuer-Fähigkeiten
        skillTree.addNode("fire", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "fire_burst_node", "fire_burst", "Feuerexplosion", "Eine explosive Feuerattacke",
                1, 3, 2, 5, null, 1, 0));
        skillTree.addNode("fire", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "fire_shield_node", "fire_shield", "Feuerschild", "Schützt mit Flammen",
                7, 4, 2, 5, new String[]{"fire_burst_node"}, 1, 1));
        skillTree.addNode("fire", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "fire_wall_node", "fire_wall", "Feuerwand", "Erschafft eine brennende Wand",
                11, 5, 3, 5, new String[]{"fire_shield_node"}, 1, 2));
        skillTree.addNode("fire", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "fire_storm_node", "fire_storm", "Feuersturm", "Regen aus Feuerbällen",
                15, 6, 3, 5, new String[]{"fire_wall_node"}, 1, 3));
        skillTree.addNode("fire", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "fire_spread_node", "fire_spread", "Feuerausbreitung", "Verbreitet Flammen in der Umgebung",
                20, 7, 3, 5, new String[]{"fire_storm_node"}, 1, 4));
        skillTree.addNode("fire", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "fire_phoenix_node", "fire_phoenix", "Phönix", "Werde kurzzeitig unverwundbar",
                25, 8, 4, 5, new String[]{"fire_spread_node"}, 1, 5));

        // Eis-Fähigkeiten
        skillTree.addNode("ice", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "ice_spike_node", "ice_spike", "Eisspeer", "Schießt scharfe Eiskristalle",
                1, 2, 1, 5, null, 2, 0));
        skillTree.addNode("ice", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "ice_freeze_node", "ice_freeze", "Eisfrost", "Verlangsamt Gegner",
                6, 3, 2, 5, new String[]{"ice_spike_node"}, 2, 1));
        skillTree.addNode("ice", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "ice_armor_node", "ice_armor", "Eispanzer", "Reduziert Schaden",
                10, 4, 2, 5, new String[]{"ice_freeze_node"}, 2, 2));
        skillTree.addNode("ice", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "ice_blizzard_node", "ice_blizzard", "Blizzard", "Kältewelle trifft Gegner",
                14, 6, 3, 5, new String[]{"ice_armor_node"}, 2, 3));
        skillTree.addNode("ice", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "ice_lance_node", "ice_lance", "Eislanze", "Durchbohrt Gegner mit eisiger Spitze",
                19, 7, 3, 5, new String[]{"ice_blizzard_node"}, 2, 4));
        skillTree.addNode("ice", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "ice_shard_node", "ice_shard", "Eissplitter", "Feuert mehrere Eissplitter",
                24, 8, 4, 5, new String[]{"ice_lance_node"}, 2, 5));

        // Blitz-Fähigkeiten
        skillTree.addNode("lightning", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "lightning_strike_node", "lightning_strike", "Blitzschlag", "Ein direkter Blitzangriff",
                4, 3, 2, 5, null, 3, 0));
        skillTree.addNode("lightning", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "lightning_chain_node", "lightning_chain", "Blitzkette", "Blitze springen zwischen Gegnern",
                8, 5, 3, 5, new String[]{"lightning_strike_node"}, 3, 1));
        skillTree.addNode("lightning", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "lightning_speed_node", "lightning_speed", "Blitzgeschwindigkeit", "Werde schneller",
                12, 4, 2, 5, new String[]{"lightning_strike_node"}, 3, 2));
        skillTree.addNode("lightning", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "lightning_shield_node", "lightning_shield", "Blitzschild", "Schlägt Angreifer zurück",
                16, 6, 3, 5, new String[]{"lightning_chain_node"}, 3, 3));
        skillTree.addNode("lightning", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "lightning_dash_node", "lightning_dash", "Blitz-Dash", "Rasante Bewegung in Blickrichtung",
                21, 7, 3, 5, new String[]{"lightning_shield_node"}, 3, 4));
        skillTree.addNode("lightning", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "lightning_orb_node", "lightning_orb", "Blitzkugel", "Eine kugelförmige Blitzentladung",
                26, 8, 4, 5, new String[]{"lightning_dash_node"}, 3, 5));

        // Erde-Fähigkeiten
        skillTree.addNode("earth", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "earth_quake_node", "earth_quake", "Erdbeben", "Erschüttert den Boden",
                5, 4, 2, 5, null, 4, 0));
        skillTree.addNode("earth", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "earth_wall_node", "earth_wall", "Erdwall", "Erschafft eine schützende Wand",
                9, 5, 3, 5, new String[]{"earth_quake_node"}, 4, 1));
        skillTree.addNode("earth", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "earth_armor_node", "earth_armor", "Erdrüstung", "Erhöht deine Rüstung",
                13, 4, 2, 5, new String[]{"earth_wall_node"}, 4, 2));
        skillTree.addNode("earth", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "earth_fist_node", "earth_fist", "Erdfaust", "Schlag mit steinerner Faust",
                17, 6, 3, 5, new String[]{"earth_armor_node"}, 4, 3));
        skillTree.addNode("earth", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "earth_pillar_node", "earth_pillar", "Erdpfeiler", "Spießt Gegner aus der Erde auf",
                23, 7, 3, 5, new String[]{"earth_fist_node"}, 4, 4));
        skillTree.addNode("earth", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "earth_quicksand_node", "earth_quicksand", "Treibsand", "Zieht Gegner in die Erde",
                28, 8, 4, 5, new String[]{"earth_pillar_node"}, 4, 5));

        // Mana-Fähigkeiten
        skillTree.addNode("mana", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "max_mana_node", "max_mana", "Mana-Pool", "Erhöht dein maximales Mana um 20 pro Level",
                1, 2, 1, 5, null, 5, 0));
        skillTree.addNode("mana", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "mana_regen_node", "mana_regen", "Mana-Regeneration", "Erhöht deine Mana-Regeneration",
                3, 3, 2, 5, new String[]{"max_mana_node"}, 5, 1));
        skillTree.addNode("mana", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "mana_shield_node", "mana_shield", "Mana-Schild", "Mana absorbiert Schaden",
                8, 4, 2, 5, new String[]{"mana_regen_node"}, 5, 2));
        skillTree.addNode("mana", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "mana_mastery_node", "mana_mastery", "Mana-Meisterschaft", "Reduziert Manakosten",
                15, 6, 3, 5, new String[]{"mana_shield_node"}, 5, 3));
        skillTree.addNode("mana", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "mana_barrier_node", "mana_barrier", "Mana-Barriere", "Mana absorbiert eingehenden Schaden",
                20, 7, 3, 5, new String[]{"mana_mastery_node"}, 5, 4));
        skillTree.addNode("mana", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "mana_infusion_node", "mana_infusion", "Mana-Infusion", "Stellt Mana schneller wieder her",
                25, 8, 4, 5, new String[]{"mana_barrier_node"}, 5, 5));

        // Verbotene Magie
        skillTree.addNode("dark", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "shadow_cloak_node", "shadow_cloak", "Schattenmantel", "Macht dich für kurze Zeit unsichtbar",
                5, 3, 2, 5, null, 6, 0));
        skillTree.addNode("dark", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "shadow_mark_node", "shadow_mark", "Mal der Verdammnis", "Markiert ein Ziel und lässt es leuchten",
                7, 4, 2, 5, new String[]{"shadow_cloak_node"}, 6, 1));
        skillTree.addNode("dark", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "soul_drain_node", "soul_drain", "Seelenentzug", "Schädigt den Gegner und heilt dich",
                9, 5, 3, 5, new String[]{"shadow_mark_node"}, 6, 2));
        skillTree.addNode("dark", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "shadow_step_node", "shadow_step", "Schritt der Schatten", "Teleportiere dich kurz",
                12, 6, 3, 5, new String[]{"soul_drain_node"}, 6, 3));
        skillTree.addNode("dark", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "dark_void_node", "dark_void", "Dunkle Leere", "Verursacht Schaden und blendet Gegner",
                18, 7, 3, 5, new String[]{"shadow_step_node"}, 6, 4));
        skillTree.addNode("dark", new de.cottisystem.zauberstab.skilltree.SkillTreeNode(
                "shadow_clone_node", "shadow_clone", "Schatten-Klon", "Erstelle einen täuschenden Klon",
                24, 8, 4, 5, new String[]{"dark_void_node"}, 6, 5));
    }

    /**
     * Überträgt ein geladenes Profil in die Live-Systeme (Skill-Tree, Mana).
     * Muss auf dem Main-Thread aufgerufen werden.
     */
    public void applyProfileToPlayer(Player player, PlayerProfile profile) {
        PlayerSkillTree playerSkillTree = skillTree.getOrCreatePlayerSkillTree(player.getUniqueId());
        playerSkillTree.setPlayerId(player.getUniqueId().toString());
        playerSkillTree.setLevel(profile.getLevel());
        playerSkillTree.setExperience(profile.getExperience());
        playerSkillTree.setExperienceForNextLevel(profile.getExperienceForNextLevel());
        playerSkillTree.setAvailableSkillPoints(profile.getSkillPoints());
        playerSkillTree.setUnlockedNodes(new HashMap<>(profile.getSkillLevels()));

        manaManager.initializeMana(player);
        applySkillTreeBonuses(player);
        manaManager.setMana(player, Math.min(profile.getManaCurrent(), manaManager.getMaxMana(player)));
    }

    /**
     * Überträgt die Live-Daten eines Spielers zurück ins Profil (für Persistenz).
     */
    public void syncPlayerToProfile(Player player) {
        if (databaseManager == null) {
            return;
        }
        databaseManager.getPlayerCache().getProfile(player.getUniqueId()).ifPresent(profile -> {
            PlayerSkillTree playerSkillTree = skillTree.getOrCreatePlayerSkillTree(player.getUniqueId());
            profile.setLevel(playerSkillTree.getLevel());
            profile.setExperience(playerSkillTree.getExperience());
            profile.setExperienceForNextLevel(playerSkillTree.getExperienceForNextLevel());
            profile.setSkillPoints(playerSkillTree.getAvailableSkillPoints());
            profile.setSkillLevels(new HashMap<>(playerSkillTree.getUnlockedNodes()));
            profile.setManaCurrent(manaManager.getMana(player));
            profile.setManaMax(manaManager.getMaxMana(player));
            profile.markDirty();
        });
    }

    /**
     * Wendet die passiven Skill-Tree-Boni (Mana-Pool, Mana-Regeneration) an.
     */
    public void applySkillTreeBonuses(Player player) {
        PlayerSkillTree playerSkillTree = skillTree.getOrCreatePlayerSkillTree(player.getUniqueId());
        int maxManaLevel = playerSkillTree.getAbilityLevel("max_mana_node");
        int regenLevel = playerSkillTree.getAbilityLevel("mana_regen_node");

        manaManager.setMaxMana(player, 100 + maxManaLevel * 20);
        manaManager.setRegenBonus(player, regenLevel);
    }

    /**
     * Registriert alle Event-Listener.
     */
    private void registerListeners() {
        // Spieler-Lifecycle-Listener
        getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(this, databaseManager.getPlayerCache()),
                this
        );
        getServer().getPluginManager().registerEvents(
                new PlayerQuitListener(this, databaseManager.getPlayerCache()),
                this
        );

        // Stab-Interaktions-Listener
        getServer().getPluginManager().registerEvents(
                new StaffClickListener(this, staffManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new StaffHoldListener(this, staffManager),
                this
        );

        // GUI-Listener
        getServer().getPluginManager().registerEvents(
                new InventoryClickListener(this),
                this
        );

        // Forge-Block-Listener
        getServer().getPluginManager().registerEvents(
                new ForgeBlockListener(this),
                this
        );

        // Mana-Regenerations-Listener
        getServer().getPluginManager().registerEvents(
                new ManaRegenListener(this, manaManager),
                this
        );

        // Todesnachrichten für Fähigkeits-Kills
        getServer().getPluginManager().registerEvents(
                new AbilityDeathListener(this),
                this
        );

        // Mana-Regenerations-Task (regeneriert auch, wenn Spieler stillstehen)
        getServer().getScheduler().runTaskTimer(this, () ->
                getServer().getOnlinePlayers().forEach(manaManager::regenerateMana),
                20L, 20L
        );

        // Persistenz-Task: Live-Daten alle 30 Sekunden in die Profile übernehmen
        getServer().getScheduler().runTaskTimer(this, () ->
                getServer().getOnlinePlayers().forEach(this::syncPlayerToProfile),
                600L, 600L
        );
    }

    /**
     * Registriert alle Befehle.
     */
    private void registerCommands() {
        ZauberstabCommand commandHandler = new ZauberstabCommand(this);
        String[] commands = {"zauberstab", "skilltree", "st", "forge", "menu", "info", "help", "level", "skill", "give"};

        for (String name : commands) {
            org.bukkit.command.PluginCommand cmd = getCommand(name);
            if (cmd != null) {
                cmd.setExecutor(commandHandler);
                cmd.setTabCompleter(commandHandler);
            } else {
                getLogger().warning("⚠️ Befehl '" + name + "' konnte nicht registriert werden!");
            }
        }
    }

    /**
     * Gibt die Instanz des Plugins zurück.
     */
    public static ZauberstabPlugin getInstance() {
        return instance;
    }

    /**
     * Gibt den ConfigManager zurück.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gibt den DatabaseManager zurück.
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Gibt den StaffManager zurück.
     */
    public StaffManager getStaffManager() {
        return staffManager;
    }

    /**
     * Gibt den AbilityLoader zurück.
     */
    public AbilityLoader getAbilityLoader() {
        return abilityLoader;
    }

    /**
     * Gibt den CooldownManager zurück.
     */
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    /**
     * Gibt den ManaManager zurück.
     */
    public ManaManager getManaManager() {
        return manaManager;
    }

    /**
     * Gibt den SkillTree zurück.
     */
    public SkillTree getSkillTree() {
        return skillTree;
    }

    /**
     * Gibt den AnimationManager zurück.
     */
    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    /**
     * Gibt den BalanceManager zurück.
     */
    public BalanceManager getBalanceManager() {
        return balanceManager;
    }

    /**
     * Gibt den AbilityExecutor zurück.
     */
    public AbilityExecutor getAbilityExecutor() {
        return abilityExecutor;
    }

}
