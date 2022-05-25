package tech.zmario.privatemessages.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tech.zmario.privatemessages.bungee.PrivateMessagesBungee;
import tech.zmario.privatemessages.bungee.enums.MessagesConfiguration;
import tech.zmario.privatemessages.bungee.enums.SettingsConfiguration;

public class SocialSpyCommand extends Command {

    private final PrivateMessagesBungee plugin;

    public SocialSpyCommand(PrivateMessagesBungee plugin) {
        super(SettingsConfiguration.COMMAND_SOCIAL_SPY_NAME.getString(), SettingsConfiguration.COMMAND_SOCIAL_SPY_PERMISSION.getString(), SettingsConfiguration.COMMAND_SOCIAL_SPY_ALIASES.getStringList().toArray(new String[0]));
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(MessagesConfiguration.NO_CONSOLE.getString());
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!player.hasPermission(SettingsConfiguration.COMMAND_SOCIAL_SPY_PERMISSION.getString())) {
            player.sendMessage(MessagesConfiguration.NO_PERMISSION.getString());
            return;
        }
        if (plugin.getStorage().hasSocialSpy(player.getUniqueId())) {
            plugin.getStorage().setSocialSpy(player.getUniqueId(), false);
            player.sendMessage(MessagesConfiguration.SOCIAL_SPY_OFF.getString());
        } else {
            plugin.getStorage().setSocialSpy(player.getUniqueId(), true);
            player.sendMessage(MessagesConfiguration.SOCIAL_SPY_ON.getString());
        }
        plugin.getDatabaseManager().updateSocialSpy(player, plugin.getStorage().hasSocialSpy(player.getUniqueId()));
    }
}
