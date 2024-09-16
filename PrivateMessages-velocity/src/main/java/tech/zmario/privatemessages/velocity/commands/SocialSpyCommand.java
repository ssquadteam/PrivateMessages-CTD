package tech.zmario.privatemessages.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import tech.zmario.privatemessages.velocity.PrivateMessagesVelocity;
import tech.zmario.privatemessages.velocity.enums.MessagesConfiguration;
import tech.zmario.privatemessages.velocity.enums.SettingsConfiguration;

public class SocialSpyCommand implements SimpleCommand {

    private final PrivateMessagesVelocity plugin;

    public SocialSpyCommand(PrivateMessagesVelocity plugin) {
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
