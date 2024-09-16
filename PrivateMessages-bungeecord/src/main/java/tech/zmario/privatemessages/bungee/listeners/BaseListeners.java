package tech.zmario.privatemessages.bungee.listeners;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import tech.zmario.privatemessages.bungee.PrivateMessagesBungee;
import tech.zmario.privatemessages.common.objects.GamePlayer;

@RequiredArgsConstructor
public class BaseListeners implements Listener {

    private final PrivateMessagesBungee plugin;

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();

        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            if (!plugin.getDatabaseManager().isPresent(player)) {
                plugin.getDatabaseManager().createPlayer(player);
            }

            GamePlayer gamePlayer = new GamePlayer(player.getUniqueId(), plugin.getDatabaseManager().getIgnoredPlayers(player));

            gamePlayer.setToggleEnabled(plugin.getDatabaseManager().getToggledStatus(player));
            gamePlayer.setSocialSpyEnabled(plugin.getDatabaseManager().getSocialSpyStatus(player));

            plugin.getStorage().getGamePlayers().put(player.getUniqueId(), gamePlayer);
        });
    }
}
