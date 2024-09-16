package tech.zmario.privatemessages.bungee.commands;

import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import tech.zmario.privatemessages.bungee.PrivateMessagesBungee;
import tech.zmario.privatemessages.bungee.enums.MessagesConfiguration;
import tech.zmario.privatemessages.bungee.enums.SettingsConfiguration;
import tech.zmario.privatemessages.bungee.utils.Utils;
import tech.zmario.privatemessages.common.storage.DataStorage;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MessageCommand extends Command implements TabExecutor {

    private final PrivateMessagesBungee plugin;

    public MessageCommand(PrivateMessagesBungee plugin) {
        super(SettingsConfiguration.COMMAND_MESSAGE_NAME.getString(), null, SettingsConfiguration.COMMAND_MESSAGE_ALIASES.getStringList().toArray(new String[0]));
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Audience audience = plugin.getAdventure().sender(sender);

        if (args.length == 1 && args[0].equalsIgnoreCase(SettingsConfiguration.COMMAND_RELOAD_NAME.getString()) && sender.hasPermission(SettingsConfiguration.COMMAND_RELOAD_PERMISSION.getString())) {
            plugin.getConfigManager().reloadAll();
            audience.sendMessage(MessagesConfiguration.CONFIGURATIONS_RELOADED.getString());
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            audience.sendMessage(MessagesConfiguration.NO_CONSOLE.getString());
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!SettingsConfiguration.COMMAND_MESSAGE_PERMISSION.getString().isEmpty() && !player.hasPermission(SettingsConfiguration.COMMAND_MESSAGE_PERMISSION.getString())) {
            audience.sendMessage(MessagesConfiguration.NO_PERMISSION.getString());
            return;
        }

        if (!player.hasPermission(SettingsConfiguration.COMMAND_MESSAGE_PERMISSION.getString())) {
            audience.sendMessage(MessagesConfiguration.NO_PERMISSION.getString());
            return;
        }

        if (args.length < 2) {
            audience.sendMessage(MessagesConfiguration.MESSAGE_USAGE.getString());
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);

        if (target == null) {
            audience.sendMessage(MessagesConfiguration.MESSAGE_PLAYER_NOT_ONLINE.getString(new String[]{"%target%", args[0]}));
            return;
        }

        DataStorage data = plugin.getStorage();

        if (data.hasIgnored(player.getUniqueId(), target.getName())) {
            audience.sendMessage(MessagesConfiguration.MESSAGE_PLAYER_IGNORED.getString(new String[]{"%target%", target.getName()}));
            data.getWaitingReply().remove(player.getUniqueId());
            return;
        }

        if (data.hasIgnored(target.getUniqueId(), player.getName())) {
            audience.sendMessage(MessagesConfiguration.MESSAGE_TARGET_IGNORED.getString(
                    new String[]{"%target%", target.getName()}));
            data.getWaitingReply().remove(player.getUniqueId());
            return;
        }

        if (data.hasMessagesToggled(player.getUniqueId())) {
            audience.sendMessage(MessagesConfiguration.MESSAGE_MESSAGES_DISABLED.getString());
            return;
        }

        if (data.hasMessagesToggled(target.getUniqueId())) {
            audience.sendMessage(MessagesConfiguration.MESSAGE_MESSAGES_DISABLED_TARGET.getString(
                    new String[]{"%target%", target.getName()}));
            return;
        }

        if (target == player) {
            audience.sendMessage(MessagesConfiguration.MESSAGE_SELF_DISABLED.getString());
            return;
        }

        Audience targetAudience = plugin.getAdventure().player(target);

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        data.getWaitingReply().put(target.getUniqueId(), player.getUniqueId());
        data.getWaitingReply().put(player.getUniqueId(), target.getUniqueId());

        audience.sendMessage(MessagesConfiguration.MESSAGE_SENDER_FORMAT.getString(
                new String[]{"%target%", target.getName()},
                new String[]{"%message%", message},
                new String[]{"%player_server%", Utils.getServerDisplay(player.getServer().getInfo(), plugin.getConfig())},
                new String[]{"%target_server%", Utils.getServerDisplay(target.getServer().getInfo(), plugin.getConfig())}));

        targetAudience.sendMessage(MessagesConfiguration.MESSAGE_TARGET_FORMAT.getString(
                new String[]{"%target%", player.getName()},
                new String[]{"%message%", message},
                new String[]{"%player_server%", Utils.getServerDisplay(target.getServer().getInfo(), plugin.getConfig())},
                new String[]{"%target_server%", Utils.getServerDisplay(player.getServer().getInfo(), plugin.getConfig())}));

        Utils.sendSpyMessage(player, data, target, message, plugin);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toSet());
        }

        return ImmutableSet.of();
    }
}
