package tech.zmario.privatemessages.bungee.enums;

import tech.zmario.privatemessages.bungee.PrivateMessagesBungee;

import java.util.List;

public enum SettingsConfiguration {

    // MySQL
    MYSQL_ENABLED("mysql.enabled"),
    MYSQL_DRIVER("mysql.driver"),
    MYSQL_HOST("mysql.host"),
    MYSQL_PORT("mysql.port"),
    MYSQL_USERNAME("mysql.username"),
    MYSQL_PASSWORD("mysql.password"),
    MYSQL_DATABASE("mysql.database"),

    COMMAND_RELOAD_NAME("commands.reload.name"),
    COMMAND_RELOAD_PERMISSION("commands.reload.permission"),

    COMMAND_MESSAGE_NAME("commands.message.name"),
    COMMAND_MESSAGE_PERMISSION("commands.message.permission"),
    COMMAND_MESSAGE_ALIASES("commands.message.aliases"),

    COMMAND_REPLY_NAME("commands.reply.name"),
    COMMAND_REPLY_PERMISSION("commands.reply.permission"),
    COMMAND_REPLY_ALIASES("commands.reply.aliases"),

    COMMAND_IGNORE_NAME("commands.ignore.name"),
    COMMAND_IGNORE_PERMISSION("commands.ignore.permission"),
    COMMAND_IGNORE_ALIASES("commands.ignore.aliases"),

    COMMAND_IGNORE_ADD_NAME("commands.ignore.add.name"),
    COMMAND_IGNORE_ADD_PERMISSION("commands.ignore.add.permission"),

    COMMAND_IGNORE_REMOVE_NAME("commands.ignore.remove.name"),
    COMMAND_IGNORE_REMOVE_PERMISSION("commands.ignore.remove.permission"),

    COMMAND_IGNORE_LIST_NAME("commands.ignore.list.name"),
    COMMAND_IGNORE_LIST_PERMISSION("commands.ignore.list.permission"),
    COMMAND_IGNORE_LIST_MAX_SIZE("commands.ignore.list.max-size"),

    COMMAND_SOCIAL_SPY_NAME("commands.social-spy.name"),
    COMMAND_SOCIAL_SPY_PERMISSION("commands.social-spy.permission"),
    COMMAND_SOCIAL_SPY_ALIASES("commands.social-spy.aliases"),

    COMMAND_TOGGLE_MESSAGES_NAME("commands.toggle-messages.name"),
    COMMAND_TOGGLE_MESSAGES_PERMISSION("commands.toggle-messages.permission"),
    COMMAND_TOGGLE_MESSAGES_ALIASES("commands.toggle-messages.aliases"),
    ;

    private final String path;
    private final PrivateMessagesBungee instance = PrivateMessagesBungee.getInstance();

    SettingsConfiguration(String path) {
        this.path = path;
    }

    public String getString() {
        return instance.getConfig().getString(path);
    }

    public int getInt() {
        return instance.getConfig().getInt(path);
    }

    public boolean getBoolean() {
        return instance.getConfig().getBoolean(path);
    }

    public List<String> getStringList() {
        return instance.getConfig().getStringList(path);
    }
}
