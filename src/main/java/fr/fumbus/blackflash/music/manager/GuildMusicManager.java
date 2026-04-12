package fr.fumbus.blackflash.music.manager;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import fr.fumbus.blackflash.music.player.TrackScheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

/**
 * Manages the music state for a single Discord guild.
 *
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@Getter
@RequiredArgsConstructor
public class GuildMusicManager {

    private final TrackScheduler trackScheduler;
    private final long guildId;
    private final LavalinkClient lavalink;

    public GuildMusicManager(long guildId, LavalinkClient lavalink) {
        this.guildId = guildId;
        this.lavalink = lavalink;
        this.trackScheduler = new TrackScheduler(this);
    }

    public void stop() {
        trackScheduler.clearQueue();

        getPlayer().ifPresent(
                player -> player.setPaused(false)
                        .setTrack(null)
                        .subscribe()
        );
    }

    public Optional<Link> getLink() {
        return Optional.ofNullable(
                this.lavalink.getLinkIfCached(this.guildId)
        );
    }

    public Optional<LavalinkPlayer> getPlayer() {
        return getLink().map(Link::getCachedPlayer);
    }
}

