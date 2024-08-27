package com.slyvr.xbedwars.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public enum DatabaseType {

    MYSQL("MySQL");

    private static final Map<String, DatabaseType> BY_NAME = new HashMap<>(1, 1F);

    static {
        for (DatabaseType type : values())
            DatabaseType.BY_NAME.put(type.name.toLowerCase(), type);
    }

    private final String name;

    DatabaseType(@NotNull String name) {
        this.name = name;
    }

    @Nullable
    public static DatabaseType getByName(@NotNull String name) {
        return name != null ? BY_NAME.get(name.toLowerCase()) : null;
    }

    @NotNull
    public String getName() {
        return name;
    }

}
