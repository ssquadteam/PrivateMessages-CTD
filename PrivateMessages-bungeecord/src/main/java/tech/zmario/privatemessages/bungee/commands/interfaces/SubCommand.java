package tech.zmario.privatemessages.bungee.commands.interfaces;

import net.md_5.bungee.api.CommandSender;

public interface SubCommand {

    void execute(CommandSender sender, String[] args);

    String getPermission();

}
