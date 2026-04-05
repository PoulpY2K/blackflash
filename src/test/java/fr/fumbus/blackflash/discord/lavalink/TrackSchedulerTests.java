package fr.fumbus.blackflash.discord.lavalink;

import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrackSchedulerTests {

    private final TrackScheduler trackScheduler = new TrackScheduler(123L);

    @Test
    void onTrackStart_accessesTrackInfoFromEvent() {
        TrackStartEvent event = mock(TrackStartEvent.class, Answers.RETURNS_DEEP_STUBS);

        trackScheduler.onTrackStart(event);

        verify(event).getTrack();
    }

    @Test
    void onTrackEnd_accessesTrackInfoFromEvent() {
        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);

        trackScheduler.onTrackEnd(event);

        verify(event).getTrack();
    }

    @Test
    void onTrackEnd_accessesEndReasonFromEvent() {
        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);

        trackScheduler.onTrackEnd(event);

        verify(event).getEndReason();
    }
}

