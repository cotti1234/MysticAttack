package de.cottisystem.zauberstab.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import de.cottisystem.zauberstab.ZauberstabPlugin;
import de.cottisystem.zauberstab.ability.AbilityExecutor;

/**
 * Listener für Spieler-Tode durch Zauberstab-Fähigkeiten.
 * Ersetzt die Todesnachricht, wenn der Tod durch eine Fähigkeit verursacht wurde.
 */
public class AbilityDeathListener implements Listener {

    private final ZauberstabPlugin plugin;

    public AbilityDeathListener(ZauberstabPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        AbilityExecutor.AbilityHit hit =
                plugin.getAbilityExecutor().getLastAbilityHit(victim.getUniqueId());
        if (hit == null) {
            return;
        }

        event.setDeathMessage("§c" + victim.getName() + " §7wurde von §c" + hit.casterName()
                + "§7s Fähigkeit §6" + hit.abilityName() + " §7getötet!");
    }
}
