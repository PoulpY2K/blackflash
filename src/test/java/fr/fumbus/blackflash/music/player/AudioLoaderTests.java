package fr.fumbus.blackflash.music.player;

import dev.arbjerg.lavalink.client.player.*;
import fr.fumbus.blackflash.music.manager.GuildMusicManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioLoaderTests {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    SlashCommandInteractionEvent event;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    GuildMusicManager guildMusicManager;

    @InjectMocks
    AudioLoader audioLoader;

    @Test
    void ontrackLoaded_attachesUserDataEnqueuesTrackAndSendsMessage() {
        TrackLoaded result = mock(TrackLoaded.class, Answers.RETURNS_DEEP_STUBS);
        Track track = result.getTrack();
        when(result.getTrack().getInfo().getTitle()).thenReturn("Test Track");
        long userId = 123L;
        when(event.getUser().getIdLong()).thenReturn(userId);

        audioLoader.ontrackLoaded(result);

        verify(track).setUserData(new UserData(userId));
        verify(guildMusicManager.getTrackScheduler()).enqueue(track);
        verify(event.getHook()).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void onPlaylistLoaded_enqueuesAllTracksAndSendsPlaylistMessage() {
        PlaylistLoaded result = mock(PlaylistLoaded.class, Answers.RETURNS_DEEP_STUBS);
        Track t1 = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        Track t2 = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        List<Track> tracks = List.of(t1, t2);
        when(result.getTracks()).thenReturn(tracks);
        when(result.getInfo().getName()).thenReturn("My Playlist");

        audioLoader.onPlaylistLoaded(result);

        verify(guildMusicManager.getTrackScheduler()).enqueuePlaylist(tracks);
        verify(event.getHook()).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void onPlaylistLoaded_whenEmptyPlaylist_sendsMessageAndDoesNotEnqueue() {
        PlaylistLoaded result = mock(PlaylistLoaded.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getTracks()).thenReturn(List.of());

        audioLoader.onPlaylistLoaded(result);

        verify(event.getHook()).sendMessageEmbeds(any(MessageEmbed.class));
        verify(guildMusicManager.getTrackScheduler(), never()).enqueuePlaylist(any());
    }

    @Test
    void onSearchResultLoaded_whenResultsExist_enqueuesFirstTrackAndSendsMessage() {
        SearchResult result = mock(SearchResult.class, Answers.RETURNS_DEEP_STUBS);
        Track firstTrack = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        Track secondTrack = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getTracks()).thenReturn(List.of(firstTrack, secondTrack));
        when(firstTrack.getInfo().getTitle()).thenReturn("Found Track");
        when(event.getUser().getIdLong()).thenReturn(42L);

        audioLoader.onSearchResultLoaded(result);

        verify(guildMusicManager.getTrackScheduler()).enqueue(firstTrack);
        verify(guildMusicManager.getTrackScheduler(), never()).enqueue(secondTrack);
        verify(event.getHook()).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void onSearchResultLoaded_whenNoResults_sendsMessageAndDoesNotEnqueue() {
        SearchResult result = mock(SearchResult.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getTracks()).thenReturn(List.of());

        audioLoader.onSearchResultLoaded(result);

        verify(event.getHook()).sendMessageEmbeds(any(MessageEmbed.class));
        verify(guildMusicManager.getTrackScheduler(), never()).enqueue(any());
    }

    @Test
    void noMatches_sendsNoMatchesFoundMessage() {
        audioLoader.noMatches();

        verify(event.getHook()).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void loadFailed_sendsFailedMessageWithExceptionDetails() {
        LoadFailed result = mock(LoadFailed.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getException().getMessage()).thenReturn("Connection refused");

        audioLoader.loadFailed(result);

        verify(event.getHook()).sendMessageEmbeds(any(MessageEmbed.class));
    }

}
