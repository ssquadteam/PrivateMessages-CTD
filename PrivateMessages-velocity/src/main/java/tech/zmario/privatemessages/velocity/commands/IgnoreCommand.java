package tech.zmario.privatemessages.velocity.commands;

import com.google.common.collect.Maps;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import tech.zmario.privatemessages.velocity.PrivateMessagesVelocity;
import tech.zmario.privatemessages.velocity.commands.interfaces.SubCommand;
import tech.zmario.privatemessages.velocity.commands.subcommands.AddSubCommand;
import tech.zmario.privatemessages.velocity.commands.subcommands.ListSubCommand;
import tech.zmario.privatemessages.velocity.commands.subcommands.RemoveSubCommand;
import tech.zmario.privatemessages.velocity.enums.MessagesConfiguration;
import tech.zmario.privatemessages.velocity.enums.SettingsConfiguration;

import java.util.Map;
import java.util.Optional;

public class IgnoreCommand implements SimpleCommand {

    private final Map<String, SubCommand> subCommands;

    public IgnoreCommand(PrivateMessagesVelocity plugin) {
        subCommands = Maps.newHashMap();
        subCommands.put(SettingsConfiguration.COMMAND_IGNORE_ADD_NAME.getString(), new AddSubCommand(plugin));
        subCommands.put(SettingsConfiguration.COMMAND_IGNORE_REMOVE_NAME.getString(), new RemoveSubCommand(plugin));
        subCommands.put(SettingsConfiguration.COMMAND_IGNORE_LIST_NAME.getString(), new ListSubCommand(plugin));
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!(source instanceof Player)) {
            source.sendMessage(MessagesConfiguration.NO_CONSOLE.getString());
            return;
        }

        Player player = (Player) source;
        String[] args = invocation.arguments();

        if (!SettingsConfiguration.COMMAND_IGNORE_PERMISSION.getString().isEmpty() && !player.hasPermission(SettingsConfiguration.COMMAND_IGNORE_PERMISSION.getString())) {
            player.sendMessage(MessagesConfiguration.NO_PERMISSION.getString());
            return;
        }

        if (args.length > 0) {
            Optional<SubCommand> subCommand = Optional.ofNullable(subCommands.get(args[0].toLowerCase()));

            if (!subCommand.isPresent()) {
                for (Component string : MessagesConfiguration.IGNORE_USAGE.getStringList()) {
                    player.sendMessage(string);
                }
                return;
            }

            if (!subCommand.get().getPermission().isEmpty() && !source.hasPermission(subCommand.get().getPermission())) {
                player.sendMessage(MessagesConfiguration.NO_PERMISSION.getString());
                return;
            }

            subCommand.get().execute(source, args);
            return;
        }

        for (Component string : MessagesConfiguration.IGNORE_USAGE.getStringList()) {
            player.sendMessage(string);
        }
    }
}
