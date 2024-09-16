package tech.zmario.privatemessages.common.objects;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GamePlayer {

    private final UUID uuid;
    private final List<String> ignoredPlayers;

    private boolean toggleEnabled = true;
    private boolean socialSpyEnabled = false;

}
