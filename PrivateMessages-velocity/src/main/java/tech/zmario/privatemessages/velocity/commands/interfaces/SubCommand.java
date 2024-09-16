package tech.zmario.privatemessages.velocity.commands.interfaces;

import com.velocitypowered.api.command.CommandSource;

public interface SubCommand {

    void execute(CommandSource sender, String[] args);

    String getPermission();

}
