package tech.zmario.privatemessages.bungee.commands.subcommands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tech.zmario.privatemessages.bungee.PrivateMessagesBungee;
import tech.zmario.privatemessages.bungee.commands.interfaces.SubCommand;
import tech.zmario.privatemessages.bungee.enums.MessagesConfiguration;
import tech.zmario.privatemessages.bungee.enums.SettingsConfiguration;

@RequiredArgsConstructor
public class AddSubCommand implements SubCommand {

    private final PrivateMessagesBungee plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (args.length < 2) {
            player.sendMessage(MessagesConfiguration.IGNORE_ADD_USAGE.getString());
            return;
        }

        if (args[1].equalsIgnoreCase(player.getName())) {
            player.sendMessage(MessagesConfiguration.IGNORE_ADD_SELF_DISABLED.getString());
            return;
        }

        if (plugin.getStorage().hasIgnored(player.getUniqueId(), args[1].toLowerCase())) {
            player.sendMessage(MessagesConfiguration.IGNORE_ADD_PLAYER_ALREADY_IGNORED.getString("%target%:" + args[1]));
            return;
        }

        plugin.getStorage().updateIgnore(player.getUniqueId(), args[1].toLowerCase(), true);
        plugin.getDatabaseManager().addIgnore(player, args[1].toLowerCase());

        player.sendMessage(MessagesConfiguration.IGNORE_ADD_PLAYER_ADDED.getString("%target%:" + args[1]));
    }

    @Override
    public String getPermission() {
        return SettingsConfiguration.COMMAND_IGNORE_ADD_PERMISSION.getString();
    }
}
