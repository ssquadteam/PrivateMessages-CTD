package tech.zmario.privatemessages.bungee.commands.subcommands;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tech.zmario.privatemessages.bungee.PrivateMessagesBungee;
import tech.zmario.privatemessages.bungee.commands.interfaces.SubCommand;
import tech.zmario.privatemessages.bungee.enums.MessagesConfiguration;
import tech.zmario.privatemessages.bungee.enums.SettingsConfiguration;

import java.util.List;

@RequiredArgsConstructor
public class ListSubCommand implements SubCommand {

    private final PrivateMessagesBungee plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        Audience audience = plugin.getAdventure().player(player);

        List<String> ignoredLists = plugin.getStorage().getGamePlayers().get(player.getUniqueId()).getIgnoredPlayers();
        int page = 0;

        if (args.length == 2) {
            try {

                page = Integer.parseInt(args[1]) - 1;

                if (page < 0) {
                    audience.sendMessage(MessagesConfiguration.IGNORE_LIST_NOT_A_NUMBER.getString());
                    return;
                }
            } catch (NumberFormatException e) {
                audience.sendMessage(MessagesConfiguration.IGNORE_LIST_NOT_A_NUMBER.getString());
                return;
            }
        }

        if (ignoredLists.isEmpty()) {
            audience.sendMessage(MessagesConfiguration.IGNORE_LIST_EMPTY.getString());
            return;
        }

        int start = page * SettingsConfiguration.COMMAND_IGNORE_LIST_MAX_SIZE.getInt();
        int end = start + SettingsConfiguration.COMMAND_IGNORE_LIST_MAX_SIZE.getInt();

        if (end > ignoredLists.size()) {
            end = ignoredLists.size();
        }

        int maxPage = ignoredLists.size() / SettingsConfiguration.COMMAND_IGNORE_LIST_MAX_SIZE.getInt() + 1;

        if (page >= maxPage) {
            audience.sendMessage(MessagesConfiguration.IGNORE_LIST_PAGE_NOT_EXIST.getString(
                    new String[]{"%page%", String.valueOf((page + 1))},
                    new String[]{"%max-page%", String.valueOf(maxPage)}));
            return;
        }

        audience.sendMessage(MessagesConfiguration.IGNORE_LIST_HEADER.getString(
                new String[]{"%page%", String.valueOf((page + 1))},
                new String[]{"%max-page%", String.valueOf(maxPage)}));

        for (int i = start; i < end; i++) {
            audience.sendMessage(MessagesConfiguration.IGNORE_LIST_LINE.getString(
                    new String[]{"%index%", String.valueOf((i + 1))},
                    new String[]{"%player%", ignoredLists.get(i)}));
        }

        audience.sendMessage(MessagesConfiguration.IGNORE_LIST_FOOTER.getString(
                new String[]{"%page%", String.valueOf((page + 1))},
                new String[]{"%max-page%", String.valueOf(maxPage)}));
    }

    @Override
    public String getPermission() {
        return SettingsConfiguration.COMMAND_IGNORE_LIST_PERMISSION.getString();
    }
}
