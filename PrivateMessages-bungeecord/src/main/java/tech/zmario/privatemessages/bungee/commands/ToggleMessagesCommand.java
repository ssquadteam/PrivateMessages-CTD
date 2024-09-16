package tech.zmario.privatemessages.bungee.commands;


import net.kyori.adventure.audience.Audience;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tech.zmario.privatemessages.bungee.PrivateMessagesBungee;
import tech.zmario.privatemessages.bungee.enums.MessagesConfiguration;
import tech.zmario.privatemessages.bungee.enums.SettingsConfiguration;

public class ToggleMessagesCommand extends Command {

    private final PrivateMessagesBungee plugin;

    public ToggleMessagesCommand(PrivateMessagesBungee plugin) {
        super(SettingsConfiguration.COMMAND_TOGGLE_MESSAGES_NAME.getString(), SettingsConfiguration.COMMAND_TOGGLE_MESSAGES_PERMISSION.getString(), SettingsConfiguration.COMMAND_TOGGLE_MESSAGES_ALIASES.getStringList().toArray(new String[0]));
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Audience audience = plugin.getAdventure().sender(sender);

        if (!(sender instanceof ProxiedPlayer)) {
            audience.sendMessage(MessagesConfiguration.NO_CONSOLE.getString());
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!SettingsConfiguration.COMMAND_TOGGLE_MESSAGES_PERMISSION.getString().isEmpty() && !player.hasPermission(SettingsConfiguration.COMMAND_TOGGLE_MESSAGES_PERMISSION.getString())) {
            audience.sendMessage(MessagesConfiguration.NO_PERMISSION.getString());
            return;
        }

        if (plugin.getStorage().hasMessagesToggled(player.getUniqueId())) { // Sono DISABILITATI
            plugin.getStorage().setMessagesToggled(player.getUniqueId(), false); // ATTIVATI
            audience.sendMessage(MessagesConfiguration.TOGGLE_MESSAGES_ON.getString());
        } else {
            plugin.getStorage().setMessagesToggled(player.getUniqueId(), true); // DISABILITATI
            audience.sendMessage(MessagesConfiguration.TOGGLE_MESSAGES_OFF.getString());
        }

        plugin.getDatabaseManager().updateMessagesToggled(player, plugin.getStorage().hasMessagesToggled(player.getUniqueId()));
    }
}
