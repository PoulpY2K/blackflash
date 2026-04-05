package fr.fumbus.blackflash.discord.lavalink;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuildMusicManagerTests {

    @Test
    void guildMusicManager_createsTrackSchedulerOnConstruction() {
        GuildMusicManager manager = new GuildMusicManager(123L);

        assertThat(manager.trackScheduler)
                .isNotNull()
                .isInstanceOf(TrackScheduler.class);
    }

    @Test
    void guildMusicManager_eachInstanceHasItsOwnTrackScheduler() {
        GuildMusicManager manager1 = new GuildMusicManager(1L);
        GuildMusicManager manager2 = new GuildMusicManager(2L);

        assertThat(manager1.trackScheduler).isNotSameAs(manager2.trackScheduler);
    }
}

