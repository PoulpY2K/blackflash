package fr.fumbus.blackflash.music.manager;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import fr.fumbus.blackflash.music.player.TrackScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuildMusicManagerTests {

    @Mock
    LavalinkClient lavalinkClient;

    @Test
    void constructor_createsAndStoresATrackScheduler() {
        try (var construction = mockConstruction(TrackScheduler.class)) {
            GuildMusicManager manager = new GuildMusicManager(123L, lavalinkClient);

            assertThat(construction.constructed()).hasSize(1);
            assertThat(manager.getTrackScheduler()).isSameAs(construction.constructed().getFirst());
        }
    }

    @Test
    void whenLinkIsNotCached_getLinkAndGetPlayerBothReturnEmpty() {
        when(lavalinkClient.getLinkIfCached(123L)).thenReturn(null);

        try (var _ = mockConstruction(TrackScheduler.class)) {
            GuildMusicManager manager = new GuildMusicManager(123L, lavalinkClient);

            assertThat(manager.getLink()).isEmpty();
            assertThat(manager.getPlayer()).isEmpty();
        }
    }

    @Test
    void whenLinkIsCached_getLinkReturnsItAndGetPlayerReturnsItsCachedPlayer() {
        Link link = mock(Link.class);
        LavalinkPlayer player = mock(LavalinkPlayer.class);
        when(link.getCachedPlayer()).thenReturn(player);
        when(lavalinkClient.getLinkIfCached(123L)).thenReturn(link);

        try (var _ = mockConstruction(TrackScheduler.class)) {
            GuildMusicManager manager = new GuildMusicManager(123L, lavalinkClient);

            assertThat(manager.getLink()).contains(link);
            assertThat(manager.getPlayer()).contains(player);
        }
    }

    @Test
    void stop_clearsQueueAndStopsPlayer() {
        Link link = mock(Link.class, Answers.RETURNS_DEEP_STUBS);
        LavalinkPlayer player = mock(LavalinkPlayer.class, Answers.RETURNS_DEEP_STUBS);
        when(link.getCachedPlayer()).thenReturn(player);
        when(lavalinkClient.getLinkIfCached(123L)).thenReturn(link);

        TrackScheduler scheduler = new TrackScheduler(null);
        scheduler.queue.offer(mock(Track.class));
        GuildMusicManager manager = new GuildMusicManager(scheduler, 123L, lavalinkClient);

        manager.stop();

        assertThat(scheduler.queue).isEmpty();
        verify(player).setPaused(false);
    }

    @Test
    void stop_clearsQueueAndDoesNotThrowWhenNoPlayerIsCached() {
        when(lavalinkClient.getLinkIfCached(123L)).thenReturn(null);

        TrackScheduler scheduler = new TrackScheduler(null);
        scheduler.queue.offer(mock(Track.class));
        GuildMusicManager manager = new GuildMusicManager(scheduler, 123L, lavalinkClient);

        manager.stop();

        assertThat(scheduler.queue).isEmpty();
    }
}

