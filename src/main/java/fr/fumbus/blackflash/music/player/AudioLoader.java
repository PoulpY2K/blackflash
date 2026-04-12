package fr.fumbus.blackflash.music.player;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import fr.fumbus.blackflash.music.manager.GuildMusicManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@RequiredArgsConstructor
public class AudioLoader extends AbstractAudioLoadResultHandler {
    private final SlashCommandInteractionEvent event;
    private final GuildMusicManager guildMusicManager;

    @Override
    public void ontrackLoaded(@NonNull TrackLoaded result) {
        final Track track = result.getTrack();
        UserData userData = new UserData(event.getUser().getIdLong());
        track.setUserData(userData);
        guildMusicManager.getTrackScheduler().enqueue(track);
        final String trackTitle = track.getInfo().getTitle();

        event.getHook().sendMessage("Added to queue: " + trackTitle + "\nRequested by: <@" + userData.requester() + '>').queue();
    }

    @Override
    public void onPlaylistLoaded(@NonNull PlaylistLoaded result) {
        final List<Track> tracks = result.getTracks();

        if (tracks.isEmpty()) {
            event.getHook().sendMessage("The playlist is empty!").queue();
            return;
        }

        event.getHook()
                .sendMessage("Added " + tracks.size() + " tracks to the queue from " + result.getInfo().getName() + "!")
                .queue();

        guildMusicManager.getTrackScheduler().enqueuePlaylist(tracks);
    }

    @Override
    public void onSearchResultLoaded(@NonNull SearchResult result) {
        final List<Track> tracks = result.getTracks();

        if (tracks.isEmpty()) {
            event.getHook().sendMessage("No tracks found!").queue();
            return;
        }

        final Track firstTrack = tracks.getFirst();

        event.getHook().sendMessage("Adding to queue: " + firstTrack.getInfo().getTitle()).queue();

        guildMusicManager.getTrackScheduler().enqueue(firstTrack);
    }

    @Override
    public void noMatches() {
        event.getHook().sendMessage("No matches found for your input!").queue();
    }

    @Override
    public void loadFailed(@NonNull LoadFailed result) {
        event.getHook().sendMessage("Failed to load track! " + result.getException().getMessage()).queue();
    }
}

