package tech.zmario.privatemessages.common.storage;

import com.google.common.collect.Maps;
import lombok.Getter;
import tech.zmario.privatemessages.common.objects.GamePlayer;

import java.util.Map;
import java.util.UUID;

@Getter
public class DataStorage {

    public Map<UUID, UUID> waitingReply = Maps.newHashMap();;
    public Map<UUID, GamePlayer> gamePlayers = Maps.newHashMap();

    public boolean hasIgnored(UUID sender, String targetName) {
        return gamePlayers.get(sender).getIgnoredPlayers().contains(targetName);
    }

    public boolean hasMessagesToggled(UUID uniqueId) {
        return gamePlayers.get(uniqueId).isToggleEnabled();
    }

    public boolean hasSocialSpy(UUID uniqueId) {
        return gamePlayers.get(uniqueId).isSocialSpyEnabled();
    }

    public void setMessagesToggled(UUID uniqueId, boolean status) {
        gamePlayers.get(uniqueId).setToggleEnabled(status);
    }

    public void setSocialSpy(UUID uniqueId, boolean status) {
        gamePlayers.get(uniqueId).setSocialSpyEnabled(status);
    }

    public void updateIgnore(UUID sender, String targetName, boolean add) {
        if (add) {
            gamePlayers.get(sender).getIgnoredPlayers().add(targetName);
        } else {
            gamePlayers.get(sender).getIgnoredPlayers().remove(targetName);
        }
    }
}
