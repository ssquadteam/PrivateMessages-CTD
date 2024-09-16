package tech.zmario.privatemessages.bungee.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tech.zmario.privatemessages.bungee.PrivateMessagesBungee;
import tech.zmario.privatemessages.bungee.enums.MessagesConfiguration;
import tech.zmario.privatemessages.bungee.enums.SettingsConfiguration;
import tech.zmario.privatemessages.common.configuration.Configuration;
import tech.zmario.privatemessages.common.storage.DataStorage;

@UtilityClass
public class Utils {

    public Component colorize(String string) {
        switch (PrivateMessagesBungee.getInstance().getMessages().getString("messages-type").toLowerCase()) {
            case "minimessage":
                return MiniMessage.miniMessage().deserialize(string);
            case "ampersand":
                return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
        }

        return Component.text(string);
    }

    public void sendSpyMessage(ProxiedPlayer player, DataStorage data, ProxiedPlayer target, String message, PrivateMessagesBungee plugin) {
        plugin.getProxy().getPlayers().stream().filter(online -> online != player && online != target &&
                online.hasPermission(SettingsConfiguration.COMMAND_RELOAD_PERMISSION.getString()) &&
                data.getGamePlayers().get(online.getUniqueId()).isSocialSpyEnabled()).forEach(online ->
                plugin.getAdventure().player(online).sendMessage(MessagesConfiguration.SOCIAL_SPY_FORMAT.getString(
                        new String[]{"%target%", target.getName()},
                        new String[]{"%player%", player.getName()},
                        new String[]{"%message%", message},
                        new String[]{"%player_server%", getServerDisplay(player.getServer().getInfo(), plugin.getConfig())},
                        new String[]{"%target_server%", getServerDisplay(target.getServer().getInfo(), plugin.getConfig())})));
    }

    public String getServerDisplay(ServerInfo server, Configuration config) {
        Configuration section;

        if ((section = config.getSection("servers-configuration")) != null &&
                section.getKeys().contains(server.getName().toLowerCase())) {
            return MiniMessage.miniMessage().serialize(
                    Component.text(section.getString(server.getName() + ".color") +
                            section.getString(server.getName() + ".display-name")));
        }

        return server.getName();
    }

}
