package fr.fumbus.blackflash.discord.lavalink;

import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Handles per-guild track lifecycle events forwarded from the Lavalink client.
 *
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@RequiredArgsConstructor
public class TrackScheduler {

    public final Queue<Track> queue = new LinkedList<>();
    private final GuildMusicManager guildMusicManager;

    public void enqueue(Track track) {
        guildMusicManager.getPlayer().ifPresentOrElse(
                player -> {
                    if (player.getTrack() == null) {
                        startTrack(track);
                    } else {
                        queue.offer(track);
                    }
                },
                () -> startTrack(track)
        );
    }

    public void enqueuePlaylist(List<Track> tracks) {
        queue.addAll(tracks);

        guildMusicManager.getPlayer().ifPresentOrElse(
                player -> {
                    if (player.getTrack() == null) {
                        startTrack(queue.poll());
                    }
                },
                () -> startTrack(queue.poll())
        );
    }

    public void onTrackStart(TrackStartEvent event) {
        // TODO: your homework: Send a message to the channel somehow, have fun!
        log.info("[Guild {}] Track started: {}", guildMusicManager.getGuildId(), event.getTrack().getInfo().getTitle());
    }

    public void onTrackEnd(TrackEndEvent event) {
        // TODO: implement queue management (e.g., play next, loop)
        log.info("[Guild {}] Track ended: {} (reason: {})", guildMusicManager.getGuildId(), event.getTrack().getInfo().getTitle(), event.getEndReason());
    }

    private void startTrack(Track track) {
        guildMusicManager.getLink().ifPresent(
                link -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(50)
                        .setEndTime(track.getInfo().getLength())
                        .subscribe()
        );
    }
}
