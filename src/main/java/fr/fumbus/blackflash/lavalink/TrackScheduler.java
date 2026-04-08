package fr.fumbus.blackflash.lavalink;

import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static java.util.Objects.nonNull;

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

    @Getter
    @Setter
    private LoopMode loopMode = LoopMode.DISABLED;

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
        log.info("[Guild {}] Track ended: {} (reason: {})", guildMusicManager.getGuildId(), event.getTrack().getInfo().getTitle(), event.getEndReason());

        // If the track ended because it finished playing, or because it was replaced by another track, we may start the next one.
        // TODO: Make it a method
        var endReason = event.getEndReason();
        if (!endReason.getMayStartNext()) {
            return;
        }

        Track endedTrack = event.getTrack();
        switch (loopMode) {
            case TRACK -> startTrack(endedTrack);
            case QUEUE -> {
                queue.offer(endedTrack);
                startNextQueueTrack();
            }
            default -> startNextQueueTrack();
        }
    }

    private void startNextQueueTrack() {
        Track next = queue.poll();
        if (nonNull(next)) {
            startTrack(next);
        }
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
