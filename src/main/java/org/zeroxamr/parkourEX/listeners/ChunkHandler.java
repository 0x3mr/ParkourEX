package org.zeroxamr.parkourEX.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.zeroxamr.parkourEX.game.GameHolograms;
import org.zeroxamr.parkourEX.game.models.ChunkAddress;
import org.zeroxamr.parkourEX.game.models.LocationMeta;

import java.util.List;

public class ChunkHandler implements Listener {
    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        ChunkAddress chunk = new ChunkAddress(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());

        GameHolograms.clearChunk(chunk);

        List<LocationMeta> locations = GameHolograms.getChunk(chunk);
        if (locations == null) return;

        GameHolograms.buildLocations(locations);
    }

    @EventHandler
    private void onChunkUnload(ChunkUnloadEvent event) {
        ChunkAddress chunk = new ChunkAddress(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());
        GameHolograms.clearChunk(chunk);
    }
}
