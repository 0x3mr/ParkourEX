package org.zeroxamr.parkourEX.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.zeroxamr.parkourEX.Main;
import org.zeroxamr.parkourEX.game.GameInstance;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.util.Pdc;
import org.zeroxamr.parkourEX.util.Shared;

public class GameListener implements Listener {
    private static Main plugin;

    public static void initialize(Main plugin) {
        GameListener.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Shared.resetPlayerInfo(e.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location oldLocation = event.getFrom();
        Location currLocation = event.getTo();

        if (oldLocation.getBlockX() == currLocation.getBlockX() &&
                oldLocation.getBlockY() == currLocation.getBlockY() &&
                oldLocation.getBlockZ() == currLocation.getBlockZ()) {
            return;
        }

        Location playerLocation = currLocation.getBlock().getLocation();
        GameInstance game = GameRegistry.getGameByLocation(playerLocation);
        if (game == null) return;

        Player player = event.getPlayer();

        game.handleParkour(player, playerLocation);
    }

    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
                && event.getCause() == EntityDamageEvent.DamageCause.FALL
                && Boolean.TRUE.equals(Pdc.getBoolean(player, "inParkour"))) {
            event.setCancelled(true);
        }
    }
}
