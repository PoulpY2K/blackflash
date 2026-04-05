package fr.fumbus.blackflash.discord.jda.slash;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import fr.fumbus.blackflash.discord.lavalink.GuildMusicManager;
import fr.fumbus.blackflash.discord.lavalink.TrackScheduler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlashCommandListenerTests {

    // RETURNS_DEEP_STUBS lets the reactive chain .on(...).subscribe(...) work without NPE
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    LavalinkClient lavalinkClient;

    @InjectMocks
    SlashCommandListener listener;

    @Test
    void slashCommandListener_extendsListenerAdapter() {
        assertThat(listener).isInstanceOf(ListenerAdapter.class);
    }

    @Test
    void slashCommandListener_musicManagersInitiallyEmpty() {
        assertThat(listener.getMusicManagers()).isEmpty();
    }

    @Test
    void init_doesNotThrow() {
        assertDoesNotThrow(() -> listener.init());
    }

    @Test
    void init_subscribesToTrackStartAndTrackEndEvents() {
        listener.init();

        verify(lavalinkClient).on(TrackStartEvent.class);
        verify(lavalinkClient).on(TrackEndEvent.class);
    }

    @Test
    void init_trackStartEventHandlerForwardsEventToMatchingGuildManager() {
        listener.init();

        long guildId = 42L;
        TrackStartEvent event = mock(TrackStartEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getGuildId()).thenReturn(guildId);

        try (MockedConstruction<TrackScheduler> schedulerConstruction = mockConstruction(TrackScheduler.class)) {
            GuildMusicManager manager = new GuildMusicManager(guildId);
            listener.getMusicManagers().put(guildId, manager);

            ArgumentCaptor<Consumer<TrackStartEvent>> captor = ArgumentCaptor.captor();
            verify(lavalinkClient.on(TrackStartEvent.class)).subscribe(captor.capture());
            captor.getValue().accept(event);

            verify(schedulerConstruction.constructed().getFirst()).onTrackStart(event);
        }
    }

    @Test
    void init_trackStartEventHandlerDoesNothingWhenGuildHasNoManager() {
        listener.init();

        ArgumentCaptor<Consumer<TrackStartEvent>> captor = ArgumentCaptor.captor();
        verify(lavalinkClient.on(TrackStartEvent.class)).subscribe(captor.capture());

        TrackStartEvent event = mock(TrackStartEvent.class, Answers.RETURNS_DEEP_STUBS);
        assertDoesNotThrow(() -> captor.getValue().accept(event));
    }

    @Test
    void init_trackEndEventHandlerForwardsEventToMatchingGuildManager() {
        listener.init();

        long guildId = 42L;
        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getGuildId()).thenReturn(guildId);

        try (MockedConstruction<TrackScheduler> schedulerConstruction = mockConstruction(TrackScheduler.class)) {
            GuildMusicManager manager = new GuildMusicManager(guildId);
            listener.getMusicManagers().put(guildId, manager);

            ArgumentCaptor<Consumer<TrackEndEvent>> captor = ArgumentCaptor.captor();
            verify(lavalinkClient.on(TrackEndEvent.class)).subscribe(captor.capture());
            captor.getValue().accept(event);

            verify(schedulerConstruction.constructed().getFirst()).onTrackEnd(event);
        }
    }

    @Test
    void init_trackEndEventHandlerDoesNothingWhenGuildHasNoManager() {
        listener.init();

        ArgumentCaptor<Consumer<TrackEndEvent>> captor = ArgumentCaptor.captor();
        verify(lavalinkClient.on(TrackEndEvent.class)).subscribe(captor.capture());

        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);
        assertDoesNotThrow(() -> captor.getValue().accept(event));
    }
}
