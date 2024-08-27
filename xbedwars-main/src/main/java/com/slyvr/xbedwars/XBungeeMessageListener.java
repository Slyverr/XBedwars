package com.slyvr.xbedwars;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class XBungeeMessageListener implements PluginMessageListener {

    private static final Map<UUID, String> PLAYER_SERVER = new HashMap<>();

    private static final byte[] SERVER_GETTER;

    static {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer");

        SERVER_GETTER = out.toByteArray();
    }

    @Nullable
    public static String getPlayerServer(@NotNull Player player) {
        player.sendPluginMessage(XBedwars.getInstance(), "BungeeCord", SERVER_GETTER);

        return PLAYER_SERVER.get(player.getUniqueId());
    }

    public static boolean connectToServer(@NotNull Player player, @NotNull String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(XBedwars.getInstance(), "BungeeCord", out.toByteArray());
        player.sendPluginMessage(XBedwars.getInstance(), "BungeeCord", SERVER_GETTER);

        String player_server = PLAYER_SERVER.get(player.getUniqueId());
        return player_server != null && player_server.equalsIgnoreCase(server);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("BungeeCord"))
            return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        String sub = in.readUTF();
        if (sub.equals("GetServer"))
            PLAYER_SERVER.put(player.getUniqueId(), in.readUTF());
    }

}
