package fr.fumbus.blackflash.music.player;

import dev.arbjerg.lavalink.client.player.*;
import fr.fumbus.blackflash.music.manager.GuildMusicManager;
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
        verify(event.getHook()).sendMessage("Added to queue: Test Track\nRequested by: <@" + userId + '>');
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
        verify(event.getHook()).sendMessage("Added 2 tracks to the queue from My Playlist!");
    }

    @Test
    void onPlaylistLoaded_whenEmptyPlaylist_sendsMessageAndDoesNotEnqueue() {
        PlaylistLoaded result = mock(PlaylistLoaded.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getTracks()).thenReturn(List.of());

        audioLoader.onPlaylistLoaded(result);

        verify(event.getHook()).sendMessage("The playlist is empty!");
        verify(guildMusicManager.getTrackScheduler(), never()).enqueuePlaylist(any());
    }

    @Test
    void onSearchResultLoaded_whenResultsExist_enqueuesFirstTrackAndSendsMessage() {
        SearchResult result = mock(SearchResult.class, Answers.RETURNS_DEEP_STUBS);
        Track firstTrack = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        Track secondTrack = mock(Track.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getTracks()).thenReturn(List.of(firstTrack, secondTrack));
        when(firstTrack.getInfo().getTitle()).thenReturn("Found Track");

        audioLoader.onSearchResultLoaded(result);

        verify(guildMusicManager.getTrackScheduler()).enqueue(firstTrack);
        verify(guildMusicManager.getTrackScheduler(), never()).enqueue(secondTrack);
        verify(event.getHook()).sendMessage("Adding to queue: Found Track");
    }

    @Test
    void onSearchResultLoaded_whenNoResults_sendsMessageAndDoesNotEnqueue() {
        SearchResult result = mock(SearchResult.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getTracks()).thenReturn(List.of());

        audioLoader.onSearchResultLoaded(result);

        verify(event.getHook()).sendMessage("No tracks found!");
        verify(guildMusicManager.getTrackScheduler(), never()).enqueue(any());
    }

    @Test
    void noMatches_sendsNoMatchesFoundMessage() {
        audioLoader.noMatches();

        verify(event.getHook()).sendMessage("No matches found for your input!");
    }

    @Test
    void loadFailed_sendsFailedMessageWithExceptionDetails() {
        LoadFailed result = mock(LoadFailed.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getException().getMessage()).thenReturn("Connection refused");

        audioLoader.loadFailed(result);

        verify(event.getHook()).sendMessage("Failed to load track! Connection refused");
    }
}

