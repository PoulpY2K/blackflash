package fr.fumbus.blackflash.lavalink;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackSchedulerTests {

    @Mock
    GuildMusicManager guildMusicManager;

    @InjectMocks
    TrackScheduler trackScheduler;

    @Test
    void clearQueue_removesAllTracksFromQueue() {
        trackScheduler.queue.offer(mock(Track.class));
        trackScheduler.queue.offer(mock(Track.class));

        trackScheduler.clearQueue();

        assertThat(trackScheduler.queue).isEmpty();
    }

    @Test
    void enqueue_startsTrackViaLinkAndDoesNotQueueWhenPlayerIsAbsent() {
        Link link = mock(Link.class, Answers.RETURNS_DEEP_STUBS);
        when(guildMusicManager.getPlayer()).thenReturn(Optional.empty());
        when(guildMusicManager.getLink()).thenReturn(Optional.of(link));
        Track track = mock(Track.class, Answers.RETURNS_DEEP_STUBS);

        trackScheduler.enqueue(track);

        assertThat(trackScheduler.queue).isEmpty();
        verify(link).createOrUpdatePlayer();
    }

    @Test
    void enqueue_startsTrackImmediatelyWhenPlayerHasNoCurrentTrack() {
        LavalinkPlayer player = mock(LavalinkPlayer.class);
        when(player.getTrack()).thenReturn(null);
        when(guildMusicManager.getPlayer()).thenReturn(Optional.of(player));
        when(guildMusicManager.getLink()).thenReturn(Optional.empty());
        Track track = mock(Track.class, Answers.RETURNS_DEEP_STUBS);

        trackScheduler.enqueue(track);

        assertThat(trackScheduler.queue).isEmpty();
        verify(guildMusicManager).getLink();
    }

    @Test
    void enqueue_addsToQueueWhenPlayerIsCurrentlyPlayingATrack() {
        LavalinkPlayer player = mock(LavalinkPlayer.class);
        when(player.getTrack()).thenReturn(mock(Track.class));
        when(guildMusicManager.getPlayer()).thenReturn(Optional.of(player));
        Track newTrack = mock(Track.class);

        trackScheduler.enqueue(newTrack);

        assertThat(trackScheduler.queue).containsExactly(newTrack);
        verify(guildMusicManager, never()).getLink();
    }

    @Test
    void enqueuePlaylist_keepsAllTracksQueuedWhenPlayerIsAlreadyPlaying() {
        LavalinkPlayer player = mock(LavalinkPlayer.class);
        when(player.getTrack()).thenReturn(mock(Track.class));
        when(guildMusicManager.getPlayer()).thenReturn(Optional.of(player));
        Track t1 = mock(Track.class);
        Track t2 = mock(Track.class);
        Track t3 = mock(Track.class);

        trackScheduler.enqueuePlaylist(List.of(t1, t2, t3));

        assertThat(trackScheduler.queue).containsExactly(t1, t2, t3);
    }

    @Test
    void enqueuePlaylist_startsFirstTrackAndQueuesRestWhenPlayerHasNoCurrentTrack() {
        LavalinkPlayer player = mock(LavalinkPlayer.class);
        when(player.getTrack()).thenReturn(null);
        when(guildMusicManager.getPlayer()).thenReturn(Optional.of(player));
        when(guildMusicManager.getLink()).thenReturn(Optional.empty());
        Track t1 = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        Track t2 = mock(Track.class, Answers.RETURNS_DEEP_STUBS);

        trackScheduler.enqueuePlaylist(List.of(t1, t2));

        assertThat(trackScheduler.queue).containsExactly(t2);
    }

    @Test
    void enqueuePlaylist_startsFirstTrackViaLinkAndQueuesRestWhenPlayerIsAbsent() {
        Link link = mock(Link.class, Answers.RETURNS_DEEP_STUBS);
        when(guildMusicManager.getPlayer()).thenReturn(Optional.empty());
        when(guildMusicManager.getLink()).thenReturn(Optional.of(link));
        Track t1 = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        Track t2 = mock(Track.class, Answers.RETURNS_DEEP_STUBS);

        trackScheduler.enqueuePlaylist(List.of(t1, t2));

        assertThat(trackScheduler.queue).containsExactly(t2);
        verify(link).createOrUpdatePlayer();
    }

    @Test
    void onTrackStart_doesNotThrow() {
        TrackStartEvent event = mock(TrackStartEvent.class, Answers.RETURNS_DEEP_STUBS);

        assertDoesNotThrow(() -> trackScheduler.onTrackStart(event));
    }

    @Test
    void onTrackEnd_doesNotThrow() {
        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);

        assertDoesNotThrow(() -> trackScheduler.onTrackEnd(event));
    }

    @Test
    void onTrackEnd_withStoppedReason_doesNotStartNextTrack() {
        trackScheduler.queue.offer(mock(Track.class));
        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getEndReason()).thenReturn(Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.STOPPED);

        trackScheduler.onTrackEnd(event);

        assertThat(trackScheduler.queue).hasSize(1);
        verify(guildMusicManager, never()).getLink();
    }

    @Test
    void onTrackEnd_withDisabledMode_startsNextTrackInQueue() {
        Link link = mock(Link.class, Answers.RETURNS_DEEP_STUBS);
        when(guildMusicManager.getLink()).thenReturn(Optional.of(link));
        Track nextTrack = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        trackScheduler.queue.offer(nextTrack);

        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getEndReason()).thenReturn(Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED);

        trackScheduler.onTrackEnd(event);

        assertThat(trackScheduler.queue).isEmpty();
        verify(link).createOrUpdatePlayer();
    }

    @Test
    void onTrackEnd_withDisabledModeAndEmptyQueue_stopsPlayback() {
        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getEndReason()).thenReturn(Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED);

        trackScheduler.onTrackEnd(event);

        assertThat(trackScheduler.queue).isEmpty();
        verify(guildMusicManager, never()).getLink();
    }

    @Test
    void onTrackEnd_withLoopTrackMode_restartsEndedTrack() {
        Link link = mock(Link.class, Answers.RETURNS_DEEP_STUBS);
        when(guildMusicManager.getLink()).thenReturn(Optional.of(link));
        trackScheduler.setLoopMode(LoopMode.TRACK);

        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getEndReason()).thenReturn(Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED);

        trackScheduler.onTrackEnd(event);

        assertThat(trackScheduler.queue).isEmpty();
        verify(link).createOrUpdatePlayer();
    }

    @Test
    void onTrackEnd_withLoopQueueMode_requeueEndedTrackAndStartsNext() {
        Link link = mock(Link.class, Answers.RETURNS_DEEP_STUBS);
        when(guildMusicManager.getLink()).thenReturn(Optional.of(link));
        trackScheduler.setLoopMode(LoopMode.QUEUE);

        Track endedTrack = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        Track nextTrack = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        trackScheduler.queue.offer(nextTrack);

        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getEndReason()).thenReturn(Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED);
        when(event.getTrack()).thenReturn(endedTrack);

        trackScheduler.onTrackEnd(event);

        assertThat(trackScheduler.queue).containsExactly(endedTrack);
        verify(link).createOrUpdatePlayer();
    }

    @Test
    void onTrackEnd_withLoopQueueModeAndEmptyQueue_loopsCurrentTrack() {
        Link link = mock(Link.class, Answers.RETURNS_DEEP_STUBS);
        when(guildMusicManager.getLink()).thenReturn(Optional.of(link));
        trackScheduler.setLoopMode(LoopMode.QUEUE);

        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getEndReason()).thenReturn(Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED);

        trackScheduler.onTrackEnd(event);

        assertThat(trackScheduler.queue).isEmpty();
        verify(link).createOrUpdatePlayer();
    }
}
