package org.zeroxamr.parkourEX.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.zeroxamr.parkourEX.Main;

import java.io.File;

public enum Config {
    CHECKPOINT_SLOT("checkpointSlot", 3),
    CHECKPOINT_ITEM("checkpointItem", "PRISMARINE_SHARD"),

    RESET_SLOT("resetSlot", 4),
    RESET_ITEM("resetItem", "RED_BED"),

    CANCEL_SLOT("cancelSlot", 5),
    CANCEL_ITEM("cancelItem", "OAK_DOOR"),

    SKIP_CHECKPOINTS("skipCheckpoints", false),
    RETURN_TO_START("returnToStart", false),
    CLEAR_ALL_EFFECTS("clearAllEffects", true),
    DISABLE_COLLISIONS("disableCollisions", true),

    VOID_TELEPORT_ENABLED("voidTeleport.enabled", false),
    VOID_TELEPORT_Y_AXIS("voidTeleport.y-axis", -100);

    private static YamlConfiguration config;

    private final String path;
    private final Object def;
    private Object value;

    Config(String path, Object def) {
        this.path = path;
        this.def = def;
    }

    public static void load() {
        config = YamlConfiguration.loadConfiguration(
                new File(Main.getPlugin().getDataFolder(), "config.yml")
        );

        for (Config setting : values()) {
            Object raw = config.get(setting.path);
            setting.value = (raw != null) ? raw : setting.def;
        }
    }

    public String stringValue() {
        return value instanceof String ? (String) value : (String) def;
    }

    public int intValue() {
        return value instanceof Number ? ((Number) value).intValue() : (int) def;
    }

    public double doubleValue() {
        return value instanceof Number ? ((Number) value).doubleValue() : (double) def;
    }

    public long longValue() {
        return value instanceof Number ? ((Number) value).longValue() : (long) def;
    }

    public boolean booleanValue() {
        return value instanceof Boolean ? (Boolean) value : (boolean) def;
    }
}
