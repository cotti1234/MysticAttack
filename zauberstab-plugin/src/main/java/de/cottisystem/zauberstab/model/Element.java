package de.cottisystem.zauberstab.model;


/**
 * Element-Typ für Fähigkeiten und Stäbe.
 * Bestimmt Bonusse und Kombinationen.
 */
public class Element {

    private String id;
    private String displayName;
    private String description;
    
    // Element-Farbe für UI (RGB)
    private int colorRed;
    private int colorGreen;
    private int colorBlue;

    public Element(String id, String displayName, String description, int colorRed, int colorGreen, int colorBlue) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.colorRed = colorRed;
        this.colorGreen = colorGreen;
        this.colorBlue = colorBlue;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getColorRed() {
        return colorRed;
    }

    public int getColorGreen() {
        return colorGreen;
    }

    public int getColorBlue() {
        return colorBlue;
    }

    // ===== SETTER FÜR ALLE FELDER =====
    public void setId(String id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setColorRed(int colorRed) {
        this.colorRed = colorRed;
    }

    public void setColorGreen(int colorGreen) {
        this.colorGreen = colorGreen;
    }

    public void setColorBlue(int colorBlue) {
        this.colorBlue = colorBlue;
    }

    /**
     * Vordefinierte Elemente
     */
    public static final Element NEUTRAL = new Element("neutral", "Elementlos", "Kein Element-Bonus", 128, 128, 128);
    public static final Element WATER = new Element("water", "Wasser", "Wasser-basierte Fähigkeiten", 0, 100, 255);
    public static final Element FIRE = new Element("fire", "Feuer", "Feuer-basierte Fähigkeiten", 255, 100, 0);
    public static final Element EARTH = new Element("earth", "Erde", "Erde-basierte Fähigkeiten", 139, 69, 19);
    public static final Element STONE = new Element("stone", "Stein/Härte", "Stein und Defensive", 105, 105, 105);
    public static final Element WIND = new Element("wind", "Wind", "Wind und Mobilität", 173, 216, 230);
    public static final Element LIGHTNING = new Element("lightning", "Blitz", "Elektrische Fähigkeiten", 255, 255, 0);
    public static final Element ICE = new Element("ice", "Eis", "Eis und Verlangsamung", 173, 216, 230);
    public static final Element DARK = new Element("dark", "Verbotene Magie", "Dunkle verbotene Kräfte", 75, 0, 130);

    @Override
    public String toString() {
        return displayName;
    }
}
