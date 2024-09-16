package tech.zmario.privatemessages.bungee.commands;

import litebans.api.Database;
import net.kyori.adventure.audience.Audience;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tech.zmario.privatemessages.bungee.PrivateMessagesBungee;
import tech.zmario.privatemessages.bungee.enums.MessagesConfiguration;
import tech.zmario.privatemessages.bungee.enums.SettingsConfiguration;
import tech.zmario.privatemessages.bungee.utils.Utils;
import tech.zmario.privatemessages.common.storage.DataStorage;

public class ReplyCommand extends Command {

    private final PrivateMessagesBungee plugin;

    public ReplyCommand(PrivateMessagesBungee plugin) {
        super(SettingsConfiguration.COMMAND_REPLY_NAME.getString(), SettingsConfiguration.COMMAND_REPLY_PERMISSION.getString(), SettingsConfiguration.COMMAND_REPLY_ALIASES.getStringList().toArray(new String[0]));
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Audience audience = plugin.getAdventure().sender(sender);

        if (!(sender instanceof ProxiedPlayer)) {
            audience.sendMessage(MessagesConfiguration.NO_CONSOLE.getString());
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!SettingsConfiguration.COMMAND_REPLY_PERMISSION.getString().isEmpty() && !player.hasPermission(SettingsConfiguration.COMMAND_REPLY_PERMISSION.getString())) {
            audience.sendMessage(MessagesConfiguration.NO_PERMISSION.getString());
            return;
        }
        DataStorage data = plugin.getStorage();

        if (!data.getWaitingReply().containsKey(player.getUniqueId())) {
            audience.sendMessage(MessagesConfiguration.REPLY_NOT_IN_CONVERSATION.getString());
            return;
        }

        if (args.length == 0) {
            audience.sendMessage(MessagesConfiguration.REPLY_USAGE.getString());
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(data.getWaitingReply().get(player.getUniqueId()));

        if (target == null) {
            audience.sendMessage(MessagesConfiguration.REPLY_PLAYER_NOT_ONLINE.getString());
            return;
        }

        if (plugin.getProxy().getPluginManager().getPlugin("LiteBans") != null && Database.get().isPlayerMuted(player.getUniqueId(), null)) {
            audience.sendMessage(MessagesConfiguration.REPLY_LITEBANS_TARGET_MUTED.getString(new String[]{"%target%", target.getName()}));
            return;
        }

        if (data.hasIgnored(player.getUniqueId(), target.getName())) {
            audience.sendMessage(MessagesConfiguration.REPLY_PLAYER_IGNORED.getString());
            data.getWaitingReply().remove(player.getUniqueId());
            return;
        }

        if (data.hasIgnored(target.getUniqueId(), player.getName())) {
            audience.sendMessage(MessagesConfiguration.REPLY_TARGET_IGNORED.getString(new String[]{"%target%", target.getName()}));
            data.getWaitingReply().remove(player.getUniqueId());
            return;
        }

        if (data.hasMessagesToggled(player.getUniqueId())) {
            audience.sendMessage(MessagesConfiguration.REPLY_MESSAGES_DISABLED.getString());
            data.getWaitingReply().remove(player.getUniqueId());
            return;
        }

        if (data.hasMessagesToggled(target.getUniqueId())) {
            audience.sendMessage(MessagesConfiguration.REPLY_MESSAGES_DISABLED_TARGET.getString(new String[]{"%target%", target.getName()}));
            data.getWaitingReply().remove(player.getUniqueId());
            return;
        }
        Audience targetAudience = plugin.getAdventure().sender(target);
        String message = String.join(" ", args);

        data.getWaitingReply().put(target.getUniqueId(), player.getUniqueId());

        audience.sendMessage(MessagesConfiguration.REPLY_SENDER_FORMAT.getString(
                new String[]{"%target%", target.getName()},
                new String[]{"%message%", message},
                new String[]{"%player_server%", Utils.getServerDisplay(player.getServer().getInfo(), plugin.getConfig())},
                new String[]{"%target_server%", Utils.getServerDisplay(target.getServer().getInfo(), plugin.getConfig())}));
        targetAudience.sendMessage(MessagesConfiguration.REPLY_TARGET_FORMAT.getString(
                new String[]{"%target%", player.getName()},
                new String[]{"%message%", message},
                new String[]{"%player_server%", Utils.getServerDisplay(target.getServer().getInfo(), plugin.getConfig())},
                new String[]{"%target_server%", Utils.getServerDisplay(player.getServer().getInfo(), plugin.getConfig())}));

        Utils.sendSpyMessage(player, data, target, message, plugin);
    }


}
