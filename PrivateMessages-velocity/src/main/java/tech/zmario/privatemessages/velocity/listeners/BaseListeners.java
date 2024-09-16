package tech.zmario.privatemessages.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import tech.zmario.privatemessages.common.objects.GamePlayer;
import tech.zmario.privatemessages.velocity.PrivateMessagesVelocity;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class BaseListeners {

    private final PrivateMessagesVelocity plugin;

    @Subscribe
    public void onJoin(PostLoginEvent e) {
        Player player = e.getPlayer();

        plugin.getProxyServer().getScheduler().buildTask(plugin, () -> {
            if (!player.isActive()) return;

            if (!plugin.getDatabaseManager().isPresent(player)) {
                plugin.getDatabaseManager().createPlayer(player);
            }

            GamePlayer gamePlayer = new GamePlayer(player.getUniqueId(), plugin.getDatabaseManager().getIgnoredPlayers(player));

            gamePlayer.setToggleEnabled(plugin.getDatabaseManager().getToggledStatus(player));
            gamePlayer.setSocialSpyEnabled(plugin.getDatabaseManager().getSocialSpyStatus(player));

            plugin.getStorage().getGamePlayers().put(player.getUniqueId(), gamePlayer);
        }).delay(1L, TimeUnit.SECONDS).schedule();
    }
}
