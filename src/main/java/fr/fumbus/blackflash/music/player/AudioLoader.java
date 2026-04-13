package fr.fumbus.blackflash.music.player;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import fr.fumbus.blackflash.discord.BotEmbeds;
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
        final String trackTitle = track.getInfo().getTitle();
        UserData userData = new UserData(event.getUser().getIdLong());
        track.setUserData(userData);
        guildMusicManager.getTrackScheduler().enqueue(track);
        event.getHook().sendMessageEmbeds(BotEmbeds.trackAdded(trackTitle, userData.requester())).queue();
    }

    @Override
    public void onPlaylistLoaded(@NonNull PlaylistLoaded result) {
        final List<Track> tracks = result.getTracks();

        if (tracks.isEmpty()) {
            event.getHook().sendMessageEmbeds(BotEmbeds.playlistEmpty()).queue();
            return;
        }

        guildMusicManager.getTrackScheduler().enqueuePlaylist(tracks);
        event.getHook().sendMessageEmbeds(BotEmbeds.playlistAdded(tracks.size(), result.getInfo().getName())).queue();
    }

    @Override
    public void onSearchResultLoaded(@NonNull SearchResult result) {
        final List<Track> tracks = result.getTracks();

        if (tracks.isEmpty()) {
            event.getHook().sendMessageEmbeds(BotEmbeds.noTracksFound()).queue();
            return;
        }

        final Track firstTrack = tracks.getFirst();
        UserData userData = new UserData(event.getUser().getIdLong());
        firstTrack.setUserData(userData);
        guildMusicManager.getTrackScheduler().enqueue(firstTrack);
        event.getHook().sendMessageEmbeds(BotEmbeds.trackAdded(firstTrack.getInfo().getTitle(), userData.requester())).queue();
    }

    @Override
    public void noMatches() {
        event.getHook().sendMessageEmbeds(BotEmbeds.noTracksFound()).queue();
    }

    @Override
    public void loadFailed(@NonNull LoadFailed result) {
        event.getHook().sendMessageEmbeds(BotEmbeds.loadFailed(result.getException().getMessage())).queue();
    }
}
