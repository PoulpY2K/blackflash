package fr.fumbus.blackflash.discord.jda.slash;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import fr.fumbus.blackflash.discord.lavalink.GuildMusicManager;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@Getter
@Component
@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {
    // TODO: add all music logic here, and split into separate services if needed
    private final LavalinkClient lavalinkClient;
    private final Map<Long, GuildMusicManager> musicManagers = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        lavalinkClient.on(TrackStartEvent.class).subscribe(event ->
                Optional.ofNullable(musicManagers.get(event.getGuildId())).ifPresent(
                        mng -> mng.trackScheduler.onTrackStart(event)
                )
        );

        lavalinkClient.on(TrackEndEvent.class).subscribe(event ->
                Optional.ofNullable(musicManagers.get(event.getGuildId())).ifPresent(
                        mng -> mng.trackScheduler.onTrackEnd(event)
                )
        );

        log.info("SlashCommandListener initialized and ready to handle slash commands.");
    }
}
