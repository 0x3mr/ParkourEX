package org.zeroxamr.parkourEX.game.models;

public record CommandMeta(
        String command,
        CommandExecutor executor,
        long delay
) {}
