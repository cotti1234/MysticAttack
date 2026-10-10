package de.cottisystem.zauberstab.util;

import org.bukkit.entity.Player;

/**
 * Utility-Klasse für Bedrock-Kompatibilität.
 * Prüft, ob ein Spieler ein Bedrock-Spieler ist und bietet Fallbacks.
 */
public class BedrockUtil {

    /**
     * Prüft, ob ein Spieler ein Bedrock-Spieler ist.
     * Nutzt Floodgate-API falls verfügbar.
     */
    public static boolean isBedrock(Player player) {
        try {
            // Versuche Floodgate-API zu nutzen
            Class<?> floodgateClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = floodgateClass.getMethod("getInstance").invoke(null);
            return (boolean) api.getClass().getMethod("isFloodgatePlayer", java.util.UUID.class)
                    .invoke(api, player.getUniqueId());
        } catch (Exception e) {
            // Floodgate nicht verfügbar oder Spieler ist Java-Spieler
            return false;
        }
    }

    /**
     * Gibt den Spieler-Typ zurück.
     */
    public static String getPlayerType(Player player) {
        return isBedrock(player) ? "Bedrock" : "Java";
    }

    /**
     * Prüft, ob ein Spieler sneakt (funktioniert auf beiden Plattformen).
     */
    public static boolean isSneaking(Player player) {
        return player.isSneaking();
    }

    /**
     * Gibt eine plattformspezifische Nachricht aus.
     */
    public static void sendPlatformMessage(Player player, String javaMessage, String bedrockMessage) {
        String message = isBedrock(player) ? bedrockMessage : javaMessage;
        player.sendMessage(message);
    }
}
