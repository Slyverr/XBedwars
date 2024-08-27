package com.slyvr.xbedwars.commands.subcommands;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public abstract class SubCommand {

    private final String name;
    private final String description;
    private final String permission;

    protected SubCommand(@NotNull String name, @NotNull String description, @NotNull String permission) {
        Preconditions.checkNotNull(name, "SubCommand's name cannot be null!");
        Preconditions.checkNotNull(description, "SubCommand's description cannot be null!");
        Preconditions.checkNotNull(permission, "SubCommand's permission cannot be null!");

        this.name = name;
        this.description = description;
        this.permission = permission;
    }


    @NotNull
    public String getName() {
        return name;
    }


    @NotNull
    public String getDescription() {
        return description;
    }


    @NotNull
    public String getPermission() {
        return permission;
    }

    @NotNull
    public abstract String getUsage();

    public abstract void perform(@NotNull Player player, @NotNull String[] args);

}