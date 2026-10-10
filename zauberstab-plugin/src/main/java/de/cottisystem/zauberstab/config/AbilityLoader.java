package de.cottisystem.zauberstab.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;
import de.cottisystem.zauberstab.model.Ability;
import de.cottisystem.zauberstab.model.Element;

import java.io.InputStream;
import java.util.*;

/**
 * Lädt Fähigkeiten aus der abilities.yml Datei.
 */
public class AbilityLoader {

    private final JavaPlugin plugin;
    private final Map<String, Ability> abilities = new HashMap<>();
    private final Map<String, Map<String, Object>> tagDefinitions = new HashMap<>();

    public AbilityLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Lädt alle Fähigkeiten aus der YAML-Datei.
     */
    public void loadAbilities() {
        try {
            InputStream input = plugin.getResource("abilities.yml");
            if (input == null) {
                plugin.getLogger().warning("abilities.yml nicht gefunden!");
                return;
            }

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);

            // Lade Tag-Definitionen
            Map<String, Object> tags = (Map<String, Object>) data.get("tags");
            if (tags != null) {
                for (Map.Entry<String, Object> entry : tags.entrySet()) {
                    tagDefinitions.put(entry.getKey(), (Map<String, Object>) entry.getValue());
                }
            }

            // Lade Fähigkeiten
            Map<String, Object> abilitiesMap = (Map<String, Object>) data.get("abilities");
            if (abilitiesMap != null) {
                for (Map.Entry<String, Object> entry : abilitiesMap.entrySet()) {
                    String abilityId = entry.getKey();
                    Map<String, Object> abilityData = (Map<String, Object>) entry.getValue();
                    
                    Ability ability = parseAbility(abilityId, abilityData);
                    if (ability != null) {
                        abilities.put(abilityId, ability);
                    }
                }
            }

            plugin.getLogger().info("✓ " + abilities.size() + " Fähigkeiten geladen");
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Laden von abilities.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parst eine Fähigkeit aus der YAML-Datei.
     */
    private Ability parseAbility(String id, Map<String, Object> data) {
        try {
            Ability ability = new Ability();
            ability.setId(id);
            ability.setDisplayName((String) data.get("name"));
            ability.setDescription((String) data.get("description"));

            // Element
            String elementId = (String) data.get("element");
            ability.setElement(getElementById(elementId));

            // Level und Kosten
            ability.setMaxLevel(((Number) data.get("max_level")).intValue());
            
            List<Integer> manaCosts = (List<Integer>) data.get("mana_cost");
            ability.setManaCostPerLevel(manaCosts.stream().mapToInt(Integer::intValue).toArray());

            List<Integer> cooldowns = (List<Integer>) data.get("cooldown");
            ability.setCooldownPerLevel(cooldowns.stream().mapToInt(Integer::intValue).toArray());

            // Base Power
            ability.setBasePower(((Number) data.get("base_power")).intValue());

            // Tags
            List<String> tagsList = (List<String>) data.get("tags");
            if (tagsList != null) {
                ability.setTags(new HashSet<>(tagsList));
            }

            return ability;
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Parsen der Fähigkeit " + id + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Gibt ein Element anhand der ID zurück.
     */
    private Element getElementById(String id) {
        return switch (id.toLowerCase()) {
            case "water" -> Element.WATER;
            case "fire" -> Element.FIRE;
            case "earth" -> Element.EARTH;
            case "stone" -> Element.STONE;
            case "wind" -> Element.WIND;
            case "lightning" -> Element.LIGHTNING;
            case "ice" -> Element.ICE;
            case "dark" -> Element.DARK;
            default -> Element.NEUTRAL;
        };
    }

    /**
     * Gibt eine Fähigkeit anhand der ID zurück.
     */
    public Optional<Ability> getAbility(String id) {
        return Optional.ofNullable(abilities.get(id));
    }

    /**
     * Gibt alle Fähigkeiten zurück.
     */
    public Collection<Ability> getAllAbilities() {
        return abilities.values();
    }

    /**
     * Gibt alle Fähigkeiten mit einem bestimmten Tag zurück.
     */
    public List<Ability> getAbilitiesByTag(String tag) {
        return abilities.values().stream()
                .filter(ability -> ability.hasTag(tag))
                .toList();
    }

    /**
     * Gibt die Tag-Definition zurück.
     */
    public Optional<Map<String, Object>> getTagDefinition(String tag) {
        return Optional.ofNullable(tagDefinitions.get(tag));
    }

    /**
     * Gibt alle Tag-Definitionen zurück.
     */
    public Map<String, Map<String, Object>> getAllTagDefinitions() {
        return new HashMap<>(tagDefinitions);
    }
}
