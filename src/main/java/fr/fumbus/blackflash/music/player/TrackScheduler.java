package fr.fumbus.blackflash.music.player;

import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.player.Track;
import fr.fumbus.blackflash.music.manager.GuildMusicManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.util.Objects.isNull;
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

    public final Queue<Track> queue = new ConcurrentLinkedDeque<>();
    private final GuildMusicManager guildMusicManager;

    @Getter
    @Setter
    private volatile LoopMode loopMode = LoopMode.DISABLED;

    @Synchronized
    public void clearQueue() {
        queue.clear();
    }

    /**
     * Skips the current track, bypassing the loop mode.
     * Starts the next queued track, or stops playback if the queue is empty.
     */
    @Synchronized
    public void skip() {
        Track next = queue.poll();
        if (nonNull(next)) {
            startTrack(next);
        } else {
            guildMusicManager.getLink().ifPresent(
                    link -> link.createOrUpdatePlayer()
                            .setTrack(null)
                            .subscribe()
            );
        }
    }

    @Synchronized
    public void enqueue(Track track) {
        guildMusicManager.getPlayer().ifPresentOrElse(
                player -> {
                    if (isNull(player.getTrack())) {
                        startTrack(track);
                    } else {
                        queue.offer(track);
                    }
                },
                () -> startTrack(track)
        );
    }

    @Synchronized
    public void enqueuePlaylist(List<Track> tracks) {
        queue.addAll(tracks);

        guildMusicManager.getPlayer().ifPresentOrElse(
                player -> {
                    if (isNull(player.getTrack())) {
                        startNextQueueTrack();
                    }
                },
                this::startNextQueueTrack
        );
    }

    @Synchronized
    public void onTrackStart(TrackStartEvent event) {
        // TODO: your homework: Send a message to the channel somehow, have fun!
        log.info("[Guild {}] Track started: {}", guildMusicManager.getGuildId(), event.getTrack().getInfo().getTitle());
    }

    @Synchronized
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
            case DISABLED -> startNextQueueTrack();
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

