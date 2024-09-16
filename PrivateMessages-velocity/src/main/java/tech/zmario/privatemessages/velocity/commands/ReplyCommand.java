package tech.zmario.privatemessages.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import litebans.api.Database;
import tech.zmario.privatemessages.common.storage.DataStorage;
import tech.zmario.privatemessages.velocity.PrivateMessagesVelocity;
import tech.zmario.privatemessages.velocity.enums.MessagesConfiguration;
import tech.zmario.privatemessages.velocity.enums.SettingsConfiguration;
import tech.zmario.privatemessages.velocity.utils.Utils;

import java.util.Optional;

public class ReplyCommand implements SimpleCommand {

    private final PrivateMessagesVelocity plugin;

    public ReplyCommand(PrivateMessagesVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player)) {
            source.sendMessage(MessagesConfiguration.NO_CONSOLE.getString());
            return;
        }

        final Player player = (Player) source;

        if (!SettingsConfiguration.COMMAND_REPLY_PERMISSION.getString().isEmpty() && !player.hasPermission(SettingsConfiguration.COMMAND_REPLY_PERMISSION.getString())) {
            player.sendMessage(MessagesConfiguration.NO_PERMISSION.getString());
            return;
        }

        final DataStorage data = plugin.getStorage();

        if (!data.getWaitingReply().containsKey(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.REPLY_NOT_IN_CONVERSATION.getString());
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 0) {
            player.sendMessage(MessagesConfiguration.REPLY_USAGE.getString());
            return;
        }

        final Optional<Player> targetOptional = plugin.getProxyServer().getPlayer(data.getWaitingReply().get(player.getUniqueId()));
        if (!targetOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.REPLY_PLAYER_NOT_ONLINE.getString());
            return;
        }

        final Player target = targetOptional.get();

        if (plugin.getProxyServer().getPluginManager().getPlugin("LiteBans").isPresent() && Database.get().isPlayerMuted(player.getUniqueId(), null)) {
            player.sendMessage(MessagesConfiguration.REPLY_LITEBANS_TARGET_MUTED.getString(new String[]{"%target%", target.getUsername()}));
            return;
        }

        if (data.hasIgnored(player.getUniqueId(), target.getUsername())) {
            player.sendMessage(MessagesConfiguration.REPLY_PLAYER_IGNORED.getString());
            data.getWaitingReply().remove(player.getUniqueId());
            return;
        }

        if (data.hasIgnored(target.getUniqueId(), player.getUsername())) {
            player.sendMessage(MessagesConfiguration.REPLY_TARGET_IGNORED.getString(new String[]{"%target%", target.getUsername()}));
            data.getWaitingReply().remove(player.getUniqueId());
            return;
        }

        if (data.hasMessagesToggled(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.REPLY_MESSAGES_DISABLED.getString());
            data.getWaitingReply().remove(player.getUniqueId());
            return;
        }

        if (data.hasMessagesToggled(target.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.REPLY_MESSAGES_DISABLED_TARGET.getString(new String[]{"%target%", target.getUsername()}));
            data.getWaitingReply().remove(player.getUniqueId());
            return;
        }
        String message = String.join(" ", args);
        data.getWaitingReply().put(target.getUniqueId(), player.getUniqueId());

        player.sendMessage(MessagesConfiguration.REPLY_SENDER_FORMAT.getString(

                new String[]{"%target%", target.getUsername()},
                new String[]{"%message%", message},
                new String[]{"%player_server%", Utils.getServerDisplay(player.getCurrentServer().get().getServerInfo(), plugin.getConfig())},
                new String[]{"%target_server%", Utils.getServerDisplay(target.getCurrentServer().get().getServerInfo(), plugin.getConfig())}));

        target.sendMessage(MessagesConfiguration.REPLY_TARGET_FORMAT.getString(
                new String[]{"%target%", player.getUsername()},
                new String[]{"%message%", message},
                new String[]{"%player_server%", Utils.getServerDisplay(target.getCurrentServer().get().getServerInfo(), plugin.getConfig())},
                new String[]{"%target_server%", Utils.getServerDisplay(player.getCurrentServer().get().getServerInfo(), plugin.getConfig())}));

        Utils.sendSpyMessage(player, data, target, message, plugin);
    }
}
