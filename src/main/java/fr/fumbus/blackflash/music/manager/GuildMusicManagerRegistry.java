package fr.fumbus.blackflash.music.manager;

import dev.arbjerg.lavalink.client.LavalinkClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring-managed registry that owns the per-guild {@link GuildMusicManager} map
 * and exposes a thread-safe {@link #getOrCreate(long)} accessor.
 *
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Component
@RequiredArgsConstructor
public class GuildMusicManagerRegistry {

    private final LavalinkClient lavalink;

    private final Map<Long, GuildMusicManager> managers = new ConcurrentHashMap<>();

    public GuildMusicManager getOrCreate(long guildId) {
        return managers.computeIfAbsent(guildId, id -> new GuildMusicManager(id, lavalink));
    }

    public Optional<GuildMusicManager> getIfPresent(long guildId) {
        return Optional.ofNullable(managers.get(guildId));
    }

    public void remove(long guildId) {
        managers.remove(guildId);
    }
}

