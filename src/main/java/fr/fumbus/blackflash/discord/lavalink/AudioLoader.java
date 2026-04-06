package fr.fumbus.blackflash.discord.lavalink;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
    public void ontrackLoaded(@NotNull TrackLoaded result) {
        final Track track = result.getTrack();
        UserData userData = new UserData(event.getUser().getIdLong());
        track.setUserData(userData);
        guildMusicManager.getTrackScheduler().enqueue(track);
        final String trackTitle = track.getInfo().getTitle();

        event.getHook().sendMessage("Added to queue: " + trackTitle + "\nRequested by: <@" + userData.requester() + '>').queue();
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result) {
        final int trackCount = result.getTracks().size();
        event.getHook()
                .sendMessage("Added " + trackCount + " tracks to the queue from " + result.getInfo().getName() + "!")
                .queue();

        guildMusicManager.getTrackScheduler().enqueuePlaylist(result.getTracks());
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult result) {
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
    public void loadFailed(@NotNull LoadFailed result) {
        event.getHook().sendMessage("Failed to load track! " + result.getException().getMessage()).queue();
    }
}
