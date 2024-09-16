package tech.zmario.privatemessages.velocity.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import tech.zmario.privatemessages.common.configuration.Configuration;
import tech.zmario.privatemessages.common.storage.DataStorage;
import tech.zmario.privatemessages.velocity.PrivateMessagesVelocity;
import tech.zmario.privatemessages.velocity.enums.MessagesConfiguration;
import tech.zmario.privatemessages.velocity.enums.SettingsConfiguration;

@UtilityClass
public class Utils {

    public Component colorize(String string) {
        switch (PrivateMessagesVelocity.getInstance().getMessages().getString("messages-type").toLowerCase()) {
            case "minimessage":
                return MiniMessage.miniMessage().deserialize(string);
            case "ampersand":
                return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
        }

        return Component.text(string);
    }

    public void sendSpyMessage(Player player, DataStorage data, Player target, String message, PrivateMessagesVelocity plugin) {

        plugin.getProxyServer().getAllPlayers().stream().filter(online -> online != player && online != target &&
                online.hasPermission(SettingsConfiguration.COMMAND_RELOAD_PERMISSION.getString()) &&
                data.getGamePlayers().get(online.getUniqueId()).isSocialSpyEnabled()).forEach(online ->
                online.sendMessage(MessagesConfiguration.SOCIAL_SPY_FORMAT.getString(
                new String[]{"%target%", target.getUsername()},
                new String[]{"%player%", player.getUsername()},
                new String[]{"%message%", message},
                new String[]{"%player_server%", getServerDisplay(player.getCurrentServer().get().getServerInfo(), plugin.getConfig())},
                new String[]{"%target_server%", getServerDisplay(target.getCurrentServer().get().getServerInfo(), plugin.getConfig())})));
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
