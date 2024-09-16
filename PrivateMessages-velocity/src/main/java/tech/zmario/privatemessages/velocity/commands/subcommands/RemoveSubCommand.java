package tech.zmario.privatemessages.velocity.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import tech.zmario.privatemessages.velocity.PrivateMessagesVelocity;
import tech.zmario.privatemessages.velocity.commands.interfaces.SubCommand;
import tech.zmario.privatemessages.velocity.enums.MessagesConfiguration;
import tech.zmario.privatemessages.velocity.enums.SettingsConfiguration;

@RequiredArgsConstructor
public class RemoveSubCommand implements SubCommand {

    private final PrivateMessagesVelocity plugin;

    @Override
    public void execute(CommandSource sender, String[] args) {
        Player player = (Player) sender;
        if (args.length < 2) {
            player.sendMessage(MessagesConfiguration.IGNORE_REMOVE_USAGE.getString());
            return;
        }

        if (player.getUsername().equalsIgnoreCase(args[1])) {
            player.sendMessage(MessagesConfiguration.IGNORE_REMOVE_SELF_DISABLED.getString());
            return;
        }

        if (!plugin.getStorage().hasIgnored(player.getUniqueId(), args[1].toLowerCase())) {
            player.sendMessage(MessagesConfiguration.IGNORE_REMOVE_PLAYER_NOT_IGNORED.getString(new String[]{"%target%", args[1]}));
            return;
        }
        plugin.getStorage().updateIgnore(player.getUniqueId(), args[1].toLowerCase(), false);
        plugin.getDatabaseManager().removeIgnore(player, args[1].toLowerCase());

        player.sendMessage(MessagesConfiguration.IGNORE_REMOVE_PLAYER_REMOVED.getString(new String[]{"%target%", args[1]}));
    }

    @Override
    public String getPermission() {
        return SettingsConfiguration.COMMAND_IGNORE_REMOVE_PERMISSION.getString();
    }
}
