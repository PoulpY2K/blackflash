package fr.fumbus.blackflash.discord.lavalink;

import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Handles per-guild track lifecycle events forwarded from the Lavalink client.
 *
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@RequiredArgsConstructor
public class TrackScheduler {

    private final long guildId;

    public void onTrackStart(TrackStartEvent event) {
        log.info("[Guild {}] Track started: {}", guildId, event.getTrack().getInfo().getTitle());
    }

    public void onTrackEnd(TrackEndEvent event) {
        // TODO: implement queue management (e.g., play next, loop)
        log.info("[Guild {}] Track ended: {} (reason: {})", guildId, event.getTrack().getInfo().getTitle(), event.getEndReason());
    }
}
