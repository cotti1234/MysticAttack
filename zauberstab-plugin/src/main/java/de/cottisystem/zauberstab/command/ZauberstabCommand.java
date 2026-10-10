package de.cottisystem.zauberstab.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import de.cottisystem.zauberstab.ZauberstabPlugin;
import de.cottisystem.zauberstab.crafting.ForgeGUI;
import de.cottisystem.zauberstab.gui.StaffMenuGUI;
import de.cottisystem.zauberstab.item.StaffType;
import de.cottisystem.zauberstab.model.Staff;
import de.cottisystem.zauberstab.skilltree.PlayerSkillTree;
import de.cottisystem.zauberstab.skilltree.SkillTreeGUI;
import de.cottisystem.zauberstab.skilltree.SkillTreeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Haupt-Command-Handler für /zauberstab.
 * Verwaltet alle Subcommands inklusive vollständiger Tab-Completion.
 */
public class ZauberstabCommand implements TabExecutor {

    private static final List<String> USER_SUBCOMMANDS = Arrays.asList("help", "info", "menu", "skilltree", "forge");
    private static final List<String> ADMIN_SUBCOMMANDS = Arrays.asList("level", "skill", "give");

    private final ZauberstabPlugin plugin;

    public ZauberstabCommand(ZauberstabPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = label.toLowerCase();

        // Top-Level-Befehle (z. B. /skilltree, /forge, /menu)
        return switch (cmd) {
            case "skilltree", "st" -> {
                if (!sender.hasPermission("zauberstab.user")) {
                    sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
                    yield true;
                }
                yield handleSkillTree(sender);
            }
            case "forge" -> {
                if (!sender.hasPermission("zauberstab.user")) {
                    sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
                    yield true;
                }
                yield handleForge(sender);
            }
            case "menu" -> {
                if (!sender.hasPermission("zauberstab.user")) {
                    sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
                    yield true;
                }
                yield handleMenu(sender);
            }
            case "info" -> handleInfo(sender, prepend(args, cmd));
            case "help" -> {
                sendHelp(sender);
                yield true;
            }
            case "level" -> handleLevel(sender, prepend(args, cmd));
            case "skill" -> handleSkill(sender, prepend(args, cmd));
            case "give" -> handleGive(sender, prepend(args, cmd));
            default -> handleMainCommand(sender, args);
        };
    }

    /**
     * Hängt ein Element am Anfang eines String-Arrays an.
     */
    private String[] prepend(String[] args, String first) {
        String[] result = new String[args.length + 1];
        result[0] = first;
        System.arraycopy(args, 0, result, 1, args.length);
        return result;
    }

    /**
     * Verarbeitet /zauberstab [subcommand] ... (Hauptbefehl mit Unterbefehlen).
     */
    private boolean handleMainCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        // Rechte-Prüfung für User-Befehle
        if (USER_SUBCOMMANDS.contains(subcommand) && !sender.hasPermission("zauberstab.user")) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl! §7(zauberstab.user)");
            return true;
        }

        return switch (subcommand) {
            case "help" -> {
                sendHelp(sender);
                yield true;
            }
            case "info" -> handleInfo(sender, args);
            case "menu" -> handleMenu(sender);
            case "skilltree" -> handleSkillTree(sender);
            case "forge" -> handleForge(sender);
            case "level" -> handleLevel(sender, args);
            case "skill" -> handleSkill(sender, args);
            case "give" -> handleGive(sender, args);
            default -> {
                sender.sendMessage("§cUnbekannter Befehl: " + subcommand + " §7(/zauberstab help)");
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = alias.toLowerCase();

        // Top-Level-Befehle: erster echter Parameter ist args[0]
        return switch (cmd) {
            case "info" -> tabInfo(sender, args);
            case "level" -> tabLevel(sender, args);
            case "skill" -> tabSkill(sender, args);
            case "give" -> tabGive(sender, args);
            default -> tabMainCommand(sender, args);
        };
    }

    private List<String> tabMainCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            if (sender.hasPermission("zauberstab.user")) {
                subcommands.addAll(USER_SUBCOMMANDS);
            }
            if (sender.hasPermission("zauberstab.level")) subcommands.add("level");
            if (sender.hasPermission("zauberstab.skill")) subcommands.add("skill");
            if (sender.hasPermission("zauberstab.give")) subcommands.add("give");
            return filterByPrefix(subcommands, args[0]);
        }

        return tabSubcommand(sender, args[0].toLowerCase(), args);
    }

    private List<String> tabSubcommand(CommandSender sender, String subcommand, String[] args) {
        return switch (subcommand) {
            case "info" -> tabInfo(sender, new String[]{subcommand}.length == args.length
                    ? new String[0] : java.util.Arrays.copyOfRange(args, 1, args.length));
            case "level" -> tabLevel(sender, shiftArgs(args));
            case "skill" -> tabSkill(sender, shiftArgs(args));
            case "give" -> tabGive(sender, shiftArgs(args));
            default -> List.of();
        };
    }

    private String[] shiftArgs(String[] args) {
        if (args.length <= 1) return new String[0];
        return java.util.Arrays.copyOfRange(args, 1, args.length);
    }

    private List<String> tabInfo(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterByPrefix(onlinePlayerNames(), args[0]);
        }
        return List.of();
    }

    private List<String> tabLevel(CommandSender sender, String[] args) {
        if (!sender.hasPermission("zauberstab.level")) return List.of();
        if (args.length == 1) {
            return filterByPrefix(onlinePlayerNames(), args[0]);
        }
        if (args.length == 2) {
            return filterByPrefix(Arrays.asList("1", "5", "10", "25"), args[1]);
        }
        return List.of();
    }

    private List<String> tabSkill(CommandSender sender, String[] args) {
        if (!sender.hasPermission("zauberstab.skill")) return List.of();
        if (args.length == 1) {
            return filterByPrefix(onlinePlayerNames(), args[0]);
        }
        if (args.length == 2) {
            List<String> nodeIds = plugin.getSkillTree().getAllNodes().stream()
                    .map(SkillTreeNode::getId)
                    .sorted()
                    .toList();
            return filterByPrefix(nodeIds, args[1]);
        }
        if (args.length == 3) {
            return filterByPrefix(Arrays.asList("0", "1", "2", "3", "4", "5"), args[2]);
        }
        return List.of();
    }

    private List<String> tabGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("zauberstab.give")) return List.of();
        if (args.length == 1) {
            return filterByPrefix(onlinePlayerNames(), args[0]);
        }
        if (args.length == 2) {
            List<String> typeIds = Arrays.stream(StaffType.values())
                    .map(StaffType::getId)
                    .toList();
            return filterByPrefix(typeIds, args[1]);
        }
        return List.of();
    }

    /**
     * Zeigt die Hilfe-Übersicht an.
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Zauberstab-Plugin Befehle ===");
        sender.sendMessage("§7/zauberstab info [player] §8- Spieler-Informationen anzeigen");
        sender.sendMessage("§7/zauberstab menu §8- Stab-Menü öffnen (Stab in der Hand)");
        sender.sendMessage("§7/zauberstab skilltree §8- Skill-Tree öffnen");
        sender.sendMessage("§7/zauberstab forge §8- Fähigkeits-Schmiede öffnen");
        if (sender.hasPermission("zauberstab.level")) {
            sender.sendMessage("§7/zauberstab level <player> <amount> §8- Skill-Punkte hinzufügen");
        }
        if (sender.hasPermission("zauberstab.skill")) {
            sender.sendMessage("§7/zauberstab skill <player> <node> <level> §8- Fähigkeit setzen");
        }
        if (sender.hasPermission("zauberstab.give")) {
            sender.sendMessage("§7/zauberstab give <player> <type> §8- Stab geben");
        }
    }

    /**
     * Behandelt den /zauberstab info Befehl.
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        Player target;

        if (args.length < 2) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cDu musst einen Spieler angeben!");
                return true;
            }
            target = player;
        } else {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cSpieler '" + args[1] + "' nicht gefunden!");
                return true;
            }
        }

        PlayerSkillTree playerSkillTree =
                plugin.getSkillTree().getOrCreatePlayerSkillTree(target.getUniqueId());

        sender.sendMessage("§6=== Zauberstab-Info: " + target.getName() + " ===");
        sender.sendMessage("§7Level: §a" + playerSkillTree.getLevel());
        sender.sendMessage("§7Skill-Punkte: §a" + playerSkillTree.getAvailableSkillPoints());
        sender.sendMessage("§7Freigeschaltete Fähigkeiten: §a" + playerSkillTree.getUnlockedAbilityCount());
        sender.sendMessage("§7Durchschnittliches Level: §a" + String.format("%.2f", playerSkillTree.getAverageAbilityLevel()));

        return true;
    }

    /**
     * Behandelt den /zauberstab menu Befehl (öffnet das Stab-Menü).
     */
    private boolean handleMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können dieses Menü öffnen!");
            return true;
        }

        Optional<Staff> staffOpt = plugin.getStaffManager().getPlayerStaff(player);
        if (staffOpt.isEmpty()) {
            player.sendMessage("§cDu musst einen Zauberstab in der Hand halten!");
            return true;
        }

        StaffMenuGUI.openStaffMenu(player, staffOpt.get());
        return true;
    }

    /**
     * Behandelt den /zauberstab skilltree Befehl (öffnet den Skill-Tree).
     */
    private boolean handleSkillTree(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können dieses Menü öffnen!");
            return true;
        }

        PlayerSkillTree playerSkillTree =
                plugin.getSkillTree().getOrCreatePlayerSkillTree(player.getUniqueId());
        SkillTreeGUI.openSkillTreeMenu(player, playerSkillTree, plugin.getSkillTree());
        return true;
    }

    /**
     * Behandelt den /zauberstab forge Befehl (öffnet die Schmiede).
     */
    private boolean handleForge(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können dieses Menü öffnen!");
            return true;
        }

        ForgeGUI.openForgeMenu(player);
        return true;
    }

    /**
     * Behandelt den /zauberstab level Befehl.
     */
    private boolean handleLevel(CommandSender sender, String[] args) {
        if (!sender.hasPermission("zauberstab.level")) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl! §7(zauberstab.level)");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cSyntax: /zauberstab level <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cSpieler '" + args[1] + "' nicht gefunden!");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                sender.sendMessage("§cDie Menge muss größer als 0 sein!");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cUngültige Zahl: " + args[2]);
            return true;
        }

        PlayerSkillTree playerSkillTree =
                plugin.getSkillTree().getOrCreatePlayerSkillTree(target.getUniqueId());
        playerSkillTree.addSkillPoints(amount);

        sender.sendMessage("§a✓ " + amount + " Skill-Punkte zu " + target.getName() + " hinzugefügt!");
        target.sendMessage("§a✓ Du hast " + amount + " Skill-Punkte erhalten!");
        return true;
    }

    /**
     * Behandelt den /zauberstab skill Befehl.
     */
    private boolean handleSkill(CommandSender sender, String[] args) {
        if (!sender.hasPermission("zauberstab.skill")) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl! §7(zauberstab.skill)");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage("§cSyntax: /zauberstab skill <player> <node> <level>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cSpieler '" + args[1] + "' nicht gefunden!");
            return true;
        }

        String nodeId = args[2];
        Optional<SkillTreeNode> nodeOpt = plugin.getSkillTree().getNode(nodeId);
        if (nodeOpt.isEmpty()) {
            sender.sendMessage("§cUnbekannter Skill-Knoten: " + nodeId);
            return true;
        }

        int maxLevel = nodeOpt.get().getMaxLevel();
        int level;
        try {
            level = Integer.parseInt(args[3]);
            if (level < 0 || level > maxLevel) {
                sender.sendMessage("§cLevel muss zwischen 0 und " + maxLevel + " sein!");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cUngültiges Level: " + args[3]);
            return true;
        }

        PlayerSkillTree playerSkillTree =
                plugin.getSkillTree().getOrCreatePlayerSkillTree(target.getUniqueId());

        if (level == 0) {
            playerSkillTree.getUnlockedNodes().remove(nodeId);
            sender.sendMessage("§a✓ Fähigkeit '" + nodeId + "' für " + target.getName() + " entfernt!");
            target.sendMessage("§c✗ Fähigkeit '" + nodeId + "' wurde entfernt!");
        } else {
            playerSkillTree.unlockAbility(nodeId, 0);
            for (int i = 1; i < level; i++) {
                playerSkillTree.levelUpAbility(nodeId, 0); // 0 Kosten für Admin
            }
            sender.sendMessage("§a✓ Fähigkeit '" + nodeId + "' auf Level " + level + " für " + target.getName() + " gesetzt!");
            target.sendMessage("§a✓ Fähigkeit '" + nodeId + "' auf Level " + level + " gesetzt!");
        }

        // Passive Boni (Mana-Pool/Regeneration) sofort anwenden
        plugin.applySkillTreeBonuses(target);

        return true;
    }

    /**
     * Behandelt den /zauberstab give Befehl (gibt einem Spieler einen Stab).
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("zauberstab.give")) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl! §7(zauberstab.give)");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cSyntax: /zauberstab give <player> <type>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cSpieler '" + args[1] + "' nicht gefunden!");
            return true;
        }

        String typeId = args[2].toLowerCase();
        StaffType staffType = Arrays.stream(StaffType.values())
                .filter(type -> type.getId().equalsIgnoreCase(typeId))
                .findFirst()
                .orElse(null);

        if (staffType == null) {
            String available = Arrays.stream(StaffType.values())
                    .map(StaffType::getId)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            sender.sendMessage("§cUnbekannter Stab-Typ: " + typeId + " §7(verfügbar: " + available + ")");
            return true;
        }

        ItemStack staffItem = plugin.getStaffManager().getItemManager().createStaffItem(staffType);
        target.getInventory().addItem(staffItem);

        sender.sendMessage("§a✓ " + staffType.getDisplayName() + " an " + target.getName() + " gegeben!");
        target.sendMessage("§a✓ Du hast einen " + staffType.getDisplayName() + " erhalten!");
        return true;
    }

    /**
     * Gibt alle Online-Spielernamen zurück.
     */
    private List<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .sorted()
                .toList();
    }

    /**
     * Filtert eine Liste nach einem Präfix (case-insensitive).
     */
    private List<String> filterByPrefix(List<String> options, String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerPrefix))
                .toList();
    }
}
