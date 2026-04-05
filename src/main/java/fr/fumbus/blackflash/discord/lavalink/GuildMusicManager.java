package fr.fumbus.blackflash.discord.lavalink;

/**
 * Manages the music state for a single Discord guild.
 *
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

public class GuildMusicManager {

    public final TrackScheduler trackScheduler;

    public GuildMusicManager(long guildId) {
        this.trackScheduler = new TrackScheduler(guildId);
    }
}
